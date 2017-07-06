package com.gi.xm.es.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gi.xm.es.pojo.Pagination;
import com.gi.xm.es.pojo.query.MergeEventQuery;
import com.gi.xm.es.util.ListUtil;
import com.gi.xm.es.view.MessageStatus;
import com.gi.xm.es.view.Result;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.join.ScoreMode;
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
public class MergeEventController {

    private static final Logger LOG = LoggerFactory.getLogger(MergeEventController.class);

    @Autowired
    private Client client;

    private static final Integer SEARCHLIMIT = 2000;

    private final String INDEX = "ctdn_merge_event";

    private final String TYPE = "merge_event";

    private static Result errorRet = new Result(MessageStatus.MISS_PARAMETER.getMessage(), MessageStatus.MISS_PARAMETER.getStatus());

    @RequestMapping(value = "mergeEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result queryMergeEvent(@RequestBody MergeEventQuery mergeEvent) {
        Result ret = new Result();
        Integer pageSize = mergeEvent.getPageSize();
        Integer pageNum = mergeEvent.getPageNo();
        SearchRequestBuilder sb = client.prepareSearch(INDEX);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //按行业
        if (ListUtil.isNotEmpty(mergeEvent.getIndustryIds())) {
            queryBuilder.must(QueryBuilders.termsQuery("industryIds", mergeEvent.getIndustryIds()));
        }
        //按title
        if (!StringUtils.isEmpty(mergeEvent.getProjTitle())) {
            mergeEvent.setProjTitle(QueryParserBase.escape(mergeEvent.getProjTitle().trim()));
            BoolQueryBuilder shoudBuilder = QueryBuilders.boolQuery();
            shoudBuilder.should(QueryBuilders.wildcardQuery("projTitle", "*" +  mergeEvent.getProjTitle() + "*"));
            shoudBuilder.should(QueryBuilders.nestedQuery("mergeSideJson",QueryBuilders.wildcardQuery("mergeSideJson.title","*"+mergeEvent.getProjTitle()+"*"), ScoreMode.None));
            shoudBuilder.minimumNumberShouldMatch(1);
            queryBuilder.must(shoudBuilder);
        }
        //股权占比
        if (ListUtil.isNotEmpty(mergeEvent.getEquityRates())) {
            queryBuilder.must(QueryBuilders.termsQuery("equityrateRange", mergeEvent.getEquityRates()));
        }
        //按币种
        if (ListUtil.isNotEmpty(mergeEvent.getCurrencyTypes())) {
            queryBuilder.must(QueryBuilders.termsQuery("currencyType", mergeEvent.getCurrencyTypes()));
        }
        //按并购状态
//        if (ListUtil.isNotEmpty(mergeEvent.getMergeStates())) {
//            queryBuilder.must(QueryBuilders.termsQuery("mergeState", mergeEvent.getMergeStates()));
//        }
        //按并购类型
//        if (ListUtil.isNotEmpty(mergeEvent.getMergeTypes())) {
//            queryBuilder.must(QueryBuilders.termsQuery("mergeType", mergeEvent.getMergeTypes()));
//        }
        //按并购结束时间
        if (!StringUtils.isEmpty(mergeEvent.getStartDate()) || !StringUtils.isEmpty(mergeEvent.getEndDate())) {
            RangeQueryBuilder rangeq = QueryBuilders.rangeQuery("mergeDate");
            if (!StringUtils.isEmpty(mergeEvent.getStartDate())) {
                rangeq.gte(mergeEvent.getStartDate());
            }
            if (!StringUtils.isEmpty(mergeEvent.getEndDate())) {
                rangeq.lte(mergeEvent.getEndDate());
            }
            queryBuilder.filter(rangeq);
        }

        if (ListUtil.isNotEmpty(mergeEvent.getCurrencyTypes())) {
            queryBuilder.must(QueryBuilders.termsQuery("currencyType", mergeEvent.getCurrencyTypes()));
        }
        //设置分页参数和请求参数
        sb.setQuery(queryBuilder);
        //求总数
        SearchResponse res = sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        Long totalHit = res.getHits().totalHits();

        if (!StringUtils.isEmpty(mergeEvent.getOrderBy())) {
            sb.addSort(mergeEvent.getOrderBy(), SortOrder.fromString(mergeEvent.getOrder()));
        } else {
            sb.addSort("mergeDate", SortOrder.DESC);
        }
        Integer tmp = pageSize;
        if (pageSize*pageNum+pageSize > SEARCHLIMIT){
            tmp =  SEARCHLIMIT - pageSize*pageNum;
        }
        sb.setFrom(pageNum*pageSize).setSize(tmp);
        //返回响应
        SearchResponse response = sb.setTypes(TYPE).setSearchType(SearchType.DEFAULT).execute().actionGet();
        SearchHits shs = response.getHits();
        List<Object> entityList = new ArrayList<>();

        for (SearchHit it : shs) {
            try {
                Map source = it.getSource();
                MergeEventQuery entity = JSON.parseObject(JSON.toJSONString(source), MergeEventQuery.class);
                //获取对应的高亮域
                Map<String, HighlightField> result = it.highlightFields();
                //从设定的高亮域中取得指定域
                //高亮company
                if (!StringUtils.isEmpty(mergeEvent.getProjTitle())) {
                    Field field1 = entity.getClass().getDeclaredField("projTitle");
                    field1.setAccessible(true);
                    Object object1 = field1.get(entity);
                    //判断是否有该属性
                    if (object1 != null) {
                        String value1 = object1.toString();
                        if (value1 != null) {
                            field1.set(entity, value1.replaceAll(mergeEvent.getProjTitle(), "<comp>" + mergeEvent.getProjTitle() + "</comp>"));
                        }
                    }
                }
                //重新构造investSideJson,使之成为json,便于解析
                Field field2 = entity.getClass().getDeclaredField("mergeSideJson");
                field2.setAccessible(true);
                Object object =  field2.get(entity);
                //判断是否有该属性
                if(object != null){
                    String value2 = object.toString();
                    String jsonStr = "{\"mergeSideJson\":"+value2+"}";
                    JSONObject obj = JSONObject.parseObject(jsonStr);
                    //高亮investSideJson
                    if(!StringUtils.isEmpty(mergeEvent.getProjTitle())) {
                        List<JSONObject> ls = (List<JSONObject>) obj.get("mergeSideJson");
                        for (JSONObject json : ls) {
                            if (json.get("title") != null) {
                                String invstor = (String) json.get("title");
                                json.put("title", invstor.replaceAll(mergeEvent.getProjTitle(), "<firm>" + mergeEvent.getProjTitle() + "</firm>"));
                            }
                        }
                    }
                    field2.set(entity,obj.toString());
                }
                entityList.add(entity);
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return errorRet;
            }
        }
        Pagination page = new Pagination();
        page.setTotal(totalHit > SEARCHLIMIT ? SEARCHLIMIT : totalHit);
        page.setTotalhit(totalHit);
        page.setRecords(entityList);
        ret = new Result(MessageStatus.OK.getMessage(), MessageStatus.OK.getStatus(), page);
        return ret;
    }
}