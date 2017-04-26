package com.gi.xm.es.controller;

import com.alibaba.fastjson.JSON;
import com.gi.xm.es.pojo.query.InvestEventQuery;
import com.gi.xm.es.pojo.query.QuitEventQuery;
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
public class QuitEventController {

    private static final Logger LOG = LoggerFactory.getLogger(QuitEventController.class);

    @Autowired
    private Client client;

    private static final Integer SEARCHLIMIT = 2000;

    private final String INDEX = "ctdn_quit_event";

    private final String TYPE = "quit_event";

    private static Result errorRet = new Result(MessageStatus.MISS_PARAMETER.getMessage(), MessageStatus.MISS_PARAMETER.getStatus());

    @RequestMapping(value="quitEvent",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result queryMergeEvent(@RequestBody QuitEventQuery quitEvent) {
        Result ret = new Result();
        Integer pageSize = quitEvent.getPageSize();
        Integer pageNum = quitEvent.getPageNo();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //按行业
        if(quitEvent.getIndustryIds() != null && !quitEvent.getIndustryIds().isEmpty() ){
            queryBuilder.must(QueryBuilders.termsQuery("industryIds",quitEvent.getIndustryIds()));
        }
        //按退出方式
        if(quitEvent.getQuitTypeList() != null && !quitEvent.getQuitType().isEmpty() ){
            queryBuilder.must(QueryBuilders.termsQuery("quitType",quitEvent.getQuitTypeList()));
        }
        //按币种
        if(quitEvent.getCurrencyList()!= null && !quitEvent.getCurrencyList().isEmpty() ){
            queryBuilder.must(QueryBuilders.termsQuery("currencyTitle",quitEvent.getCurrencyList()));
        }
        //按title
        if(quitEvent.getCompany() != null){
            queryBuilder.should(QueryBuilders.wildcardQuery("projTitle","*"+quitEvent.getCompany()+"*"));
            queryBuilder.should(QueryBuilders.wildcardQuery("mergeSideJson","*"+quitEvent.getQuitSideJson()+"*"));
        }
        //按退出时间
        if(quitEvent.getStartDate() != null || quitEvent.getEndDate()!= null){
            RangeQueryBuilder rangeq = QueryBuilders.rangeQuery("quitDate");
            if(quitEvent.getStartDate() != null ){
                rangeq.gte(quitEvent.getStartDate());
            }
            if(quitEvent.getEndDate() != null ){
                rangeq.lte(quitEvent.getEndDate());
            }
            queryBuilder.filter(rangeq);
        }
        //按地区
        if(quitEvent.getDistrictIds() != null && !quitEvent.getDistrictIds().isEmpty()){
            queryBuilder.must(QueryBuilders.termsQuery("districtId",quitEvent.getDistrictIds()));
        }
        if(quitEvent.getDistrictSubIds() != null && !quitEvent.getDistrictSubIds().isEmpty()){
            queryBuilder.must(QueryBuilders.termsQuery("districtSubId",quitEvent.getDistrictSubIds()));
        }
        //设置分页参数和请求参数
        SearchRequestBuilder sb = client.prepareSearch(INDEX);
        sb.setQuery(queryBuilder);
        //求总数
        SearchResponse res =sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        Long  totalHit = res.getHits().totalHits();

        if(quitEvent.getOrder() != null){
            sb.addSort(quitEvent.getOrderBy(), SortOrder.fromString(quitEvent.getOrderBy()));
        }else {
            sb.addSort("quitDate", SortOrder.DESC);
        }
        sb.setFrom(pageNum);
        sb.setSize(pageSize);
        //返回响应
        SearchResponse response =sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        SearchHits shs = response.getHits();
        List<Object> entityList = new ArrayList<>();
        for (SearchHit it : shs) {
            Map source = it.getSource();
            QuitEventQuery entity =  JSON.parseObject(JSON.toJSONString(source),QuitEventQuery.class);
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
                    String rep = "<span class = 'highlight'>"+entity.getCompany()+"</span>";
                    //替换
                    field.set(entity, value.replaceAll(entity.getCompany(),rep));
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