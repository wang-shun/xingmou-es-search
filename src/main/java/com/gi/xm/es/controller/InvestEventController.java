package com.gi.xm.es.controller;

import com.alibaba.fastjson.JSON;
import com.gi.xm.es.pojo.query.InvestEventQuery;
import com.gi.xm.es.util.ListUtil;
import com.gi.xm.es.view.MessageStatus;
import com.gi.xm.es.view.Pagination;
import com.gi.xm.es.view.Result;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class InvestEventController {

    private static final Logger LOG = LoggerFactory.getLogger(InvestEventController.class);

    @Autowired
    private Client client;

    private static final Integer SEARCHLIMIT = 2000;

    private final String INDEX = "ctdn_invest_event";

    private final String TYPE = "invest_event";

    private static Result errorRet = new Result(MessageStatus.MISS_PARAMETER.getMessage(), MessageStatus.MISS_PARAMETER.getStatus());

    @RequestMapping(value="investEvent",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result queryInvestEvent(@RequestBody InvestEventQuery investEvent) {
        Result ret = new Result();
        Integer pageSize = investEvent.getPageSize();
        Integer pageNum = investEvent.getPageNo();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchRequestBuilder sb = client.prepareSearch(INDEX);
        //按行业
        if(ListUtil.isNotEmpty(investEvent.getIndustryIds())){
            queryBuilder.must(QueryBuilders.termsQuery("industryIds",investEvent.getIndustryIds()));
        }
        //按title
        if(!StringUtils.isEmpty(investEvent.getCompany())){
            investEvent.setCompany(investEvent.getCompany().trim());
            BoolQueryBuilder shoudBuilder = QueryBuilders.boolQuery();
            shoudBuilder.should(QueryBuilders.wildcardQuery("company","*"+investEvent.getCompany()+"*"));
            shoudBuilder.should(QueryBuilders.wildcardQuery("investSideJson","*"+investEvent.getCompany()+"*"));
            //设置高亮
            HighlightBuilder ch = new HighlightBuilder().field("company").field("investSideJson");
            sb.highlighter(ch);
            shoudBuilder.minimumNumberShouldMatch(1);

            queryBuilder.must(shoudBuilder);
        }
        //按round
        if(ListUtil.isNotEmpty(investEvent.getInvestRounds())){
            queryBuilder.must(QueryBuilders.termsQuery("round",investEvent.getInvestRounds()));
        }
        //按investdate
        if(!StringUtils.isEmpty(investEvent.getStartDate()) || !StringUtils.isEmpty(investEvent.getEndDate())){
            RangeQueryBuilder rangeq = QueryBuilders.rangeQuery("investDate");
            if(!StringUtils.isEmpty(investEvent.getStartDate())){
                rangeq.gte(investEvent.getStartDate());
            }
            if(!StringUtils.isEmpty(investEvent.getEndDate())){
                rangeq.lte(investEvent.getEndDate());
            }
            queryBuilder.filter(rangeq);
        }

        //按地区
        if (ListUtil.isNotEmpty(investEvent.getDistrictIds())&&ListUtil.isNotEmpty(investEvent.getDistrictSubIds())) {
            BoolQueryBuilder shoudBuilder = QueryBuilders.boolQuery();
            shoudBuilder.should(QueryBuilders.termsQuery("districtId", investEvent.getDistrictIds()));
            shoudBuilder.should(QueryBuilders.termsQuery("districtSubId", investEvent.getDistrictSubIds()));
            shoudBuilder.minimumNumberShouldMatch(1);
            queryBuilder.must(shoudBuilder);
        }else if (ListUtil.isNotEmpty(investEvent.getDistrictIds())) {
            queryBuilder.must(QueryBuilders.termsQuery("districtId", investEvent.getDistrictIds()));
        }else if (ListUtil.isNotEmpty(investEvent.getDistrictSubIds())) {
            queryBuilder.must(QueryBuilders.termsQuery("districtSubId", investEvent.getDistrictSubIds()));
        }

        if(ListUtil.isNotEmpty(investEvent.getCurrencyTypes())){
            queryBuilder.must(QueryBuilders.termsQuery("currencyType",investEvent.getCurrencyTypes()));
        }
        //设置分页参数和请求参数
        sb.setQuery(queryBuilder);
        if(!StringUtils.isEmpty(investEvent.getOrderBy())){
            sb.addSort("investDate", SortOrder.fromString(investEvent.getOrder()));
        }else{
            sb.addSort("investDate", SortOrder.DESC);
        }

        //求总数
        SearchResponse res =sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        Long  totalHit = res.getHits().totalHits();
        sb.setFrom(pageNum*pageSize).setSize(pageSize);
        //返回响应
        SearchResponse response =sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        SearchHits shs = response.getHits();
        List<Object> entityList = new ArrayList<>();
        for (SearchHit it : shs) {
            Map source = it.getSource();
            InvestEventQuery entity =  JSON.parseObject(JSON.toJSONString(source),InvestEventQuery.class);
            //获取对应的高亮域
            Map<String, HighlightField> result = it.highlightFields();
            //从设定的高亮域中取得指定域
            for (Map.Entry<String, HighlightField> entry : result.entrySet()) {
                String key = entry.getKey();
                try {
                    //获得高亮字段的原值
                    Field field = entity.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    String value = field.get(entity).toString();
                    //获得搜索关键字  加高亮标签
                    if(key.equals("company")){
                        field.set(entity, value.replaceAll(investEvent.getCompany(), "<comp>"+investEvent.getCompany()+"</comp>"));;
                    }else{
                        field.set(entity, value.replaceAll(investEvent.getCompany(), "<firm>"+investEvent.getCompany()+"</firm>"));
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                    return errorRet;
                }
            }
            entityList.add(entity);
        }
        Pagination page = new Pagination();
        page.setTotal(totalHit>SEARCHLIMIT?SEARCHLIMIT:totalHit);
        page.setTotalhit(totalHit);
        page.setRecords(entityList);
        ret = new Result(MessageStatus.OK.getMessage(), MessageStatus.OK.getStatus(), page);
        return ret;
    }
}