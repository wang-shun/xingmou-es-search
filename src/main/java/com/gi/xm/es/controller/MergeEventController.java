package com.gi.xm.es.controller;

import com.alibaba.fastjson.JSON;
import com.gi.xm.es.pojo.query.InvestEventQuery;
import com.gi.xm.es.pojo.query.MergeEventQuery;
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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/search/")
public class MergeEventController {

    private static final Logger LOG = LoggerFactory.getLogger(MergeEventController.class);

    @Autowired
    private Client client;

    private static final Integer SEARCHLIMIT = 2000;

    private final String INDEX = "ctdn_merge_event";

    private final String TYPE = "merge_event";

    private static Result errorRet = new Result(MessageStatus.MISS_PARAMETER.getMessage(), MessageStatus.MISS_PARAMETER.getStatus());

    @RequestMapping(value="mergeEvent",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result queryMergeEvent(@RequestBody MergeEventQuery mergeEvent) {
        Result ret = new Result();
        Integer pageSize = mergeEvent.getPageSize();
        Integer pageNum = mergeEvent.getPageNo();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //按行业
        if(mergeEvent.getIndustryIds() != null && !mergeEvent.getIndustryIds().isEmpty() ){
            queryBuilder.must(QueryBuilders.termsQuery("industryIds",mergeEvent.getIndustryIds()));
        }
        //按title
        if(mergeEvent.getProjTitle() != null){
            queryBuilder.should(QueryBuilders.wildcardQuery("projTitle","*"+mergeEvent.getProjTitle()+"*"));
            queryBuilder.should(QueryBuilders.wildcardQuery("mergeSideJson","*"+mergeEvent.getProjTitle()+"*"));
        }
        //股权占比
        if(mergeEvent.getEquityRateList() != null && !mergeEvent.getEquityRateList().isEmpty() ){
            queryBuilder.must(QueryBuilders.termsQuery("equityRateNum",mergeEvent.getEquityRateList()));
        }

        //按并购结束时间
        if(mergeEvent.getStartDate() != null || mergeEvent.getEndDate()!= null){
            RangeQueryBuilder rangeq = QueryBuilders.rangeQuery("mergeEndDate");
            if(mergeEvent.getStartDate() != null ){
                rangeq.gte(mergeEvent.getStartDate());
            }
            if(mergeEvent.getEndDate() != null ){
                rangeq.lte(mergeEvent.getEndDate());
            }
            queryBuilder.filter(rangeq);
        }

        if(mergeEvent.getCurrencyTitle() != null){
            queryBuilder.must(QueryBuilders.termsQuery("currencyTitle",mergeEvent.getCurrencyList()));
        }
        //设置分页参数和请求参数
        SearchRequestBuilder sb = client.prepareSearch(INDEX);
        sb.setQuery(queryBuilder);
        //求总数
        SearchResponse res =sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        Long  totalHit = res.getHits().totalHits();

        if(mergeEvent.getOrder() != null){
            sb.addSort(mergeEvent.getOrderBy(), SortOrder.fromString(mergeEvent.getOrderBy()));
        }else {
            sb.addSort("mergeOrderDate", SortOrder.DESC);
        }
        sb.setFrom(pageNum);
        sb.setSize(pageSize);
        //返回响应
        SearchResponse response =sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        SearchHits shs = response.getHits();
        List<Object> entityList = new ArrayList<>();
        for (SearchHit it : shs) {
            Map source = it.getSource();
            MergeEventQuery entity =  JSON.parseObject(JSON.toJSONString(source),MergeEventQuery.class);
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
                    //获得搜索关键字
                    String rep = "<span class = 'highlight'>"+entity.getProjTitle()+"</span>";
                    //替换
                    field.set(entity, value.replaceAll(entity.getProjTitle(),rep));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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