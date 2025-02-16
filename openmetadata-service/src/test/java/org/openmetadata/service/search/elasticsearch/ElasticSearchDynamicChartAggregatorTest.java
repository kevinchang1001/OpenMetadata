package org.openmetadata.service.search.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmetadata.service.search.elasticsearch.dataInsightAggregators.ElasticSearchDynamicChartAggregatorInterface.getDateHistogramByFormula;

import es.org.elasticsearch.action.search.SearchRequest;
import es.org.elasticsearch.action.search.SearchResponse;
import es.org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import es.org.elasticsearch.plugins.spi.NamedXContentProvider;
import es.org.elasticsearch.search.aggregations.Aggregation;
import es.org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import es.org.elasticsearch.search.aggregations.bucket.filter.ParsedFilters;
import es.org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import es.org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import es.org.elasticsearch.search.aggregations.bucket.histogram.ParsedHistogram;
import es.org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange;
import es.org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import es.org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import es.org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import es.org.elasticsearch.search.aggregations.metrics.ParsedMax;
import es.org.elasticsearch.search.aggregations.metrics.ParsedMin;
import es.org.elasticsearch.search.aggregations.metrics.ParsedSum;
import es.org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import es.org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import es.org.elasticsearch.search.aggregations.pipeline.ParsedBucketMetricValue;
import es.org.elasticsearch.search.builder.SearchSourceBuilder;
import es.org.elasticsearch.search.suggest.Suggest;
import es.org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import es.org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import es.org.elasticsearch.search.suggest.term.TermSuggestion;
import es.org.elasticsearch.xcontent.ContextParser;
import es.org.elasticsearch.xcontent.DeprecationHandler;
import es.org.elasticsearch.xcontent.NamedXContentRegistry;
import es.org.elasticsearch.xcontent.ParseField;
import es.org.elasticsearch.xcontent.XContentFactory;
import es.org.elasticsearch.xcontent.XContentParser;
import es.org.elasticsearch.xcontent.json.JsonXContent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.junit.Test;
import org.openmetadata.schema.dataInsight.custom.DataInsightCustomChart;
import org.openmetadata.schema.dataInsight.custom.DataInsightCustomChartResult;
import org.openmetadata.schema.dataInsight.custom.DataInsightCustomChartResultList;
import org.openmetadata.schema.dataInsight.custom.FormulaHolder;
import org.openmetadata.service.OpenMetadataApplicationTest;
import org.openmetadata.service.search.elasticsearch.dataInsightAggregators.ElasticSearchDynamicChartAggregatorFactory;
import org.openmetadata.service.search.elasticsearch.dataInsightAggregators.ElasticSearchDynamicChartAggregatorInterface;

public class ElasticSearchDynamicChartAggregatorTest extends OpenMetadataApplicationTest {

  static final long START = 1721082271000l;
  static final long END = 1721592271000l;

  private boolean compareRequest(String expectedJsonReq, Map<String, String> chartDetails)
      throws IOException {
    XContentParser parser =
        XContentFactory.xContent(expectedJsonReq)
            .createParser(
                ElasticSearchClient.xContentRegistry,
                LoggingDeprecationHandler.INSTANCE,
                expectedJsonReq);
    SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.fromXContent(parser);

    // Create a SearchRequest and set the SearchSourceBuilder
    SearchRequest expectedSearchRequest =
        new SearchRequest().source(searchSourceBuilder).indices("di-data-assets");
    expectedSearchRequest.source(searchSourceBuilder);
    DataInsightCustomChart chart =
        new DataInsightCustomChart().withName("random_chart_name").withChartDetails(chartDetails);
    ElasticSearchDynamicChartAggregatorInterface aggregator =
        ElasticSearchDynamicChartAggregatorFactory.getAggregator(chart);
    List<FormulaHolder> formulas = new ArrayList<>();
    es.org.elasticsearch.action.search.SearchRequest searchRequest =
        aggregator.prepareSearchRequest(chart, START, END, formulas);

    return expectedSearchRequest.equals(searchRequest);
  }

  @Test
  public void testFieldChartRequestCount() throws IOException {
    String cardString1 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-21T01:34:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}";
    Map<String, String> summaryCard1 = new LinkedHashMap<>();
    summaryCard1.put("type", "SummaryCard");
    summaryCard1.put("field", "id.keyword");
    summaryCard1.put("function", "count");
    assertTrue(compareRequest(cardString1, summaryCard1));

    String lineString =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}";
    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("field", "id.keyword");
    lineChart.put("function", "count");
    assertTrue(compareRequest(lineString, lineChart));

    String lineString1 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}}}";
    Map<String, String> lineChart1 = new LinkedHashMap<>();
    lineChart1.put("type", "LineChart");
    lineChart1.put("field", "id.keyword");
    lineChart1.put("function", "count");
    lineChart1.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString1, lineChart1));

    String lineString2 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"filer\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":{\"value\":\"admin\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}}}";
    Map<String, String> lineChart2 = new LinkedHashMap<>();
    lineChart2.put("type", "LineChart");
    lineChart2.put("field", "id.keyword");
    lineChart2.put("function", "count");
    lineChart2.put(
        "filter",
        "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":\"admin\"}}]}}]}}}");
    assertTrue(compareRequest(lineString2, lineChart2));
  }

  @Test
  public void testFormulaChartRequest() throws IOException {
    String cardString =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-21T01:34:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}";
    Map<String, String> summaryCard = new LinkedHashMap<>();
    summaryCard.put("type", "SummaryCard");
    summaryCard.put("formula", "count(k='id.keyword')");
    assertTrue(compareRequest(cardString, summaryCard));

    String lineString =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}";
    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("formula", "count(k='id.keyword')");
    assertTrue(compareRequest(lineString, lineChart));

    String lineString1 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}},\"version1\":{\"sum\":{\"field\":\"version\"}}}}}}";
    Map<String, String> lineChart1 = new LinkedHashMap<>();
    lineChart1.put("type", "LineChart");
    lineChart1.put("formula", "count(k='id.keyword')+sum(k='version')");
    assertTrue(compareRequest(lineString1, lineChart1));

    String lineString2 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}},\"id.keyword1\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}";
    Map<String, String> lineChart2 = new LinkedHashMap<>();
    lineChart2.put("type", "LineChart");
    lineChart2.put("formula", "count(k='id.keyword')+count(k='id.keyword')");
    assertTrue(compareRequest(lineString2, lineChart2));

    String lineString3 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}},\"id.keyword1\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}}}";
    Map<String, String> lineChart3 = new LinkedHashMap<>();
    lineChart3.put("type", "LineChart");
    lineChart3.put("formula", "count(k='id.keyword')+count(k='id.keyword')");
    lineChart3.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString3, lineChart3));

    String lineString4 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"filer0\":{\"filter\":{\"query_string\":{\"query\":\"hasDescription: 1\",\"fields\":[],\"type\":\"best_fields\",\"default_operator\":\"or\",\"max_determinized_states\":10000,\"enable_position_increments\":true,\"fuzziness\":\"AUTO\",\"fuzzy_prefix_length\":0,\"fuzzy_max_expansions\":50,\"phrase_slop\":0,\"escape\":false,\"auto_generate_synonyms_phrase_query\":true,\"fuzzy_transpositions\":true,\"boost\":1.0}},\"aggregations\":{\"id.keyword0\":{\"value_count\":{\"field\":\"id.keyword\"}}}},\"filer1\":{\"filter\":{\"query_string\":{\"query\":\"owner.name.keyword: *\",\"fields\":[],\"type\":\"best_fields\",\"default_operator\":\"or\",\"max_determinized_states\":10000,\"enable_position_increments\":true,\"fuzziness\":\"AUTO\",\"fuzzy_prefix_length\":0,\"fuzzy_max_expansions\":50,\"phrase_slop\":0,\"escape\":false,\"auto_generate_synonyms_phrase_query\":true,\"fuzzy_transpositions\":true,\"boost\":1.0}},\"aggregations\":{\"id.keyword1\":{\"value_count\":{\"field\":\"id.keyword\"}}}}}}}}}}";
    Map<String, String> lineChart4 = new LinkedHashMap<>();
    lineChart4.put("type", "LineChart");
    lineChart4.put(
        "formula",
        "count(k='id.keyword',q='hasDescription: 1')+count(k='id.keyword',q='owner.name.keyword: *')");
    lineChart4.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString4, lineChart4));
  }

  @Test
  public void testFieldChartRequestSum() throws IOException {
    String cardString =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-21T01:34:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"sum\":{\"field\":\"version\"}}}}}}";
    Map<String, String> summaryCard = new LinkedHashMap<>();
    summaryCard.put("type", "SummaryCard");
    summaryCard.put("field", "version");
    summaryCard.put("function", "sum");
    assertTrue(compareRequest(cardString, summaryCard));

    String lineString =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"sum\":{\"field\":\"version\"}}}}}}";
    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("field", "version");
    lineChart.put("function", "sum");
    assertTrue(compareRequest(lineString, lineChart));

    String lineString1 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"sum\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart1 = new LinkedHashMap<>();
    lineChart1.put("type", "LineChart");
    lineChart1.put("field", "version");
    lineChart1.put("function", "sum");
    lineChart1.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString1, lineChart1));

    String lineString2 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"filer\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":{\"value\":\"admin\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"version0\":{\"sum\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart2 = new LinkedHashMap<>();
    lineChart2.put("type", "LineChart");
    lineChart2.put("field", "version");
    lineChart2.put("function", "sum");
    lineChart2.put(
        "filter",
        "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":\"admin\"}}]}}]}}}");
    assertTrue(compareRequest(lineString2, lineChart2));
  }

  @Test
  public void testFieldChartRequestAvg() throws IOException {
    String cardString =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-21T01:34:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"avg\":{\"field\":\"version\"}}}}}}";
    Map<String, String> summaryCard = new LinkedHashMap<>();
    summaryCard.put("type", "SummaryCard");
    summaryCard.put("field", "version");
    summaryCard.put("function", "avg");
    assertTrue(compareRequest(cardString, summaryCard));

    String lineString =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"avg\":{\"field\":\"version\"}}}}}}";
    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("field", "version");
    lineChart.put("function", "avg");
    assertTrue(compareRequest(lineString, lineChart));

    String lineString1 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"avg\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart1 = new LinkedHashMap<>();
    lineChart1.put("type", "LineChart");
    lineChart1.put("field", "version");
    lineChart1.put("function", "avg");
    lineChart1.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString1, lineChart1));

    String lineString2 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"filer\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":{\"value\":\"admin\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"version0\":{\"avg\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart2 = new LinkedHashMap<>();
    lineChart2.put("type", "LineChart");
    lineChart2.put("field", "version");
    lineChart2.put("function", "avg");
    lineChart2.put(
        "filter",
        "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":\"admin\"}}]}}]}}}");
    assertTrue(compareRequest(lineString2, lineChart2));
  }

  @Test
  public void testFieldChartRequestMin() throws IOException {
    String cardString =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-21T01:34:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"min\":{\"field\":\"version\"}}}}}}";
    Map<String, String> summaryCard = new LinkedHashMap<>();
    summaryCard.put("type", "SummaryCard");
    summaryCard.put("field", "version");
    summaryCard.put("function", "min");
    assertTrue(compareRequest(cardString, summaryCard));

    String lineString =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"min\":{\"field\":\"version\"}}}}}}";
    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("field", "version");
    lineChart.put("function", "min");
    assertTrue(compareRequest(lineString, lineChart));

    String lineString1 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"min\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart1 = new LinkedHashMap<>();
    lineChart1.put("type", "LineChart");
    lineChart1.put("field", "version");
    lineChart1.put("function", "min");
    lineChart1.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString1, lineChart1));

    String lineString2 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"filer\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":{\"value\":\"admin\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"version0\":{\"min\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart2 = new LinkedHashMap<>();
    lineChart2.put("type", "LineChart");
    lineChart2.put("field", "version");
    lineChart2.put("function", "min");
    lineChart2.put(
        "filter",
        "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":\"admin\"}}]}}]}}}");
    assertTrue(compareRequest(lineString2, lineChart2));
  }

  @Test
  public void testFieldChartRequestMax() throws IOException {
    String cardString =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-21T01:34:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"max\":{\"field\":\"version\"}}}}}}";
    Map<String, String> summaryCard = new LinkedHashMap<>();
    summaryCard.put("type", "SummaryCard");
    summaryCard.put("field", "version");
    summaryCard.put("function", "max");
    assertTrue(compareRequest(cardString, summaryCard));

    String lineString =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"max\":{\"field\":\"version\"}}}}}}";
    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("field", "version");
    lineChart.put("function", "max");
    assertTrue(compareRequest(lineString, lineChart));

    String lineString1 =
        "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"0\":{\"terms\":{\"field\":\"entityType.keyword\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"version0\":{\"max\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart1 = new LinkedHashMap<>();
    lineChart1.put("type", "LineChart");
    lineChart1.put("field", "version");
    lineChart1.put("function", "max");
    lineChart1.put("groupBy", "entityType.keyword");
    assertTrue(compareRequest(lineString1, lineChart1));

    String lineString2 =
        "{\"query\":{\"range\":{\"@timestamp\":{\"from\":\"2024-07-16T03:54:31Z\",\"to\":\"2024-07-23T01:34:31Z\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"aggregations\":{\"1\":{\"date_histogram\":{\"field\":\"@timestamp\",\"calendar_interval\":\"1d\",\"offset\":0,\"order\":{\"_key\":\"asc\"},\"keyed\":false,\"min_doc_count\":0},\"aggregations\":{\"filer\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":{\"value\":\"admin\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"version0\":{\"max\":{\"field\":\"version\"}}}}}}}}";
    Map<String, String> lineChart2 = new LinkedHashMap<>();
    lineChart2.put("type", "LineChart");
    lineChart2.put("field", "version");
    lineChart2.put("function", "max");
    lineChart2.put(
        "filter",
        "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"owner.displayName.keyword\":\"admin\"}}]}}]}}}");
    assertTrue(compareRequest(lineString2, lineChart2));
  }

  public static SearchResponse getSearchResponseFromJson(String jsonResponse) throws IOException {
    NamedXContentRegistry registry = new NamedXContentRegistry(getDefaultNamedXContents());
    XContentParser parser =
        JsonXContent.jsonXContent.createParser(
            registry, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, jsonResponse);
    return SearchResponse.fromXContent(parser);
  }

  private static List<NamedXContentRegistry.Entry> getProvidedNamedXContents() {
    List<NamedXContentRegistry.Entry> entries = new ArrayList<>();

    for (NamedXContentProvider service : ServiceLoader.load(NamedXContentProvider.class)) {
      entries.addAll(service.getNamedXContentParsers());
    }

    return entries;
  }

  private static List<NamedXContentRegistry.Entry> getDefaultNamedXContents() {
    Map<String, ContextParser<Object, ? extends Aggregation>> map = new HashMap<>();
    map.put("min", (p, c) -> ParsedMin.fromXContent(p, (String) c));
    map.put("max", (p, c) -> ParsedMax.fromXContent(p, (String) c));
    map.put("sum", (p, c) -> ParsedSum.fromXContent(p, (String) c));
    map.put("avg", (p, c) -> ParsedAvg.fromXContent(p, (String) c));
    map.put("sterms", (p, c) -> ParsedStringTerms.fromXContent(p, (String) c));
    map.put("value_count", (p, c) -> ParsedValueCount.fromXContent(p, (String) c));
    map.put("bucket_metric_value", (p, c) -> ParsedBucketMetricValue.fromXContent(p, (String) c));
    map.put("histogram", (p, c) -> ParsedHistogram.fromXContent(p, (String) c));
    map.put("date_histogram", (p, c) -> ParsedDateHistogram.fromXContent(p, (String) c));
    map.put("filter", (p, c) -> ParsedFilter.fromXContent(p, (String) c));
    map.put("range", (p, c) -> ParsedRange.fromXContent(p, (String) c));
    map.put("date_range", (p, c) -> ParsedDateRange.fromXContent(p, (String) c));
    map.put("filters", (p, c) -> ParsedFilters.fromXContent(p, (String) c));
    map.put("top_hits", (p, c) -> ParsedTopHits.fromXContent(p, (String) c));
    List<NamedXContentRegistry.Entry> entries =
        map.entrySet().stream()
            .map(
                (entry) ->
                    new NamedXContentRegistry.Entry(
                        Aggregation.class,
                        new ParseField((String) entry.getKey()),
                        entry.getValue()))
            .collect(Collectors.toList());
    entries.add(
        new NamedXContentRegistry.Entry(
            Suggest.Suggestion.class,
            new ParseField("term"),
            (parser, context) -> TermSuggestion.fromXContent(parser, (String) context)));
    entries.add(
        new NamedXContentRegistry.Entry(
            Suggest.Suggestion.class,
            new ParseField("phrase"),
            (parser, context) -> PhraseSuggestion.fromXContent(parser, (String) context)));
    entries.add(
        new NamedXContentRegistry.Entry(
            Suggest.Suggestion.class,
            new ParseField("completion"),
            (parser, context) -> CompletionSuggestion.fromXContent(parser, (String) context)));

    return entries;
  }

  private boolean compareResponse(
      String expectedJsonRes,
      Map<String, String> chartDetails,
      String formula,
      List<DataInsightCustomChartResult> expectedResultList)
      throws IOException {
    SearchResponse response = getSearchResponseFromJson(expectedJsonRes);
    DataInsightCustomChart chart =
        new DataInsightCustomChart().withName("random_chart_name").withChartDetails(chartDetails);
    ElasticSearchDynamicChartAggregatorInterface aggregator =
        ElasticSearchDynamicChartAggregatorFactory.getAggregator(chart);
    List<FormulaHolder> formulas = new ArrayList<>();
    if (formula != null) {
      getDateHistogramByFormula(formula, new DateHistogramAggregationBuilder("demo"), formulas);
    }
    DataInsightCustomChartResultList resultList =
        aggregator.processSearchResponse(chart, response, formulas);
    DataInsightCustomChartResultList expectedResult =
        new DataInsightCustomChartResultList().withResults(expectedResultList);
    return resultList.equals(expectedResult);
  }

  @Test
  public void testSearchResponseProcessor() throws IOException {
    String sampleResponse1 =
        "{\"took\":26,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":132,\"relation\":\"eq\"},\"max_score\":null,\"hits\":[]},\"aggregations\":{\"date_histogram#1\":{\"buckets\":[{\"key_as_string\":\"2024-07-21T00:00:00.000Z\",\"key\":1721520000000,\"doc_count\":54,\"value_count#id.keyword0\":{\"value\":54}},{\"key_as_string\":\"2024-07-22T00:00:00.000Z\",\"key\":1721606400000,\"doc_count\":78,\"value_count#id.keyword0\":{\"value\":78}}]}}}";
    Map<String, String> summaryCard = new LinkedHashMap<>();
    summaryCard.put("type", "SummaryCard");
    summaryCard.put("formula", "count(k='id.keyword')");
    List<DataInsightCustomChartResult> resultList = new ArrayList<>();
    resultList.add(new DataInsightCustomChartResult().withCount(78d).withDay(1721586600000d));
    assertTrue(compareResponse(sampleResponse1, summaryCard, "count(k='id.keyword')", resultList));

    Map<String, String> summaryCardFunc = new LinkedHashMap<>();
    summaryCardFunc.put("type", "SummaryCard");
    summaryCardFunc.put("function", "count");
    summaryCardFunc.put("field", "id.keyword");
    assertTrue(compareResponse(sampleResponse1, summaryCardFunc, null, resultList));

    Map<String, String> lineChart = new LinkedHashMap<>();
    lineChart.put("type", "LineChart");
    lineChart.put("formula", "count(k='id.keyword')");
    List<DataInsightCustomChartResult> resultListLine = new ArrayList<>();
    resultListLine.add(new DataInsightCustomChartResult().withCount(54d).withDay(1.7215002E12));
    resultListLine.add(new DataInsightCustomChartResult().withCount(78d).withDay(1721586600000d));
    assertTrue(
        compareResponse(sampleResponse1, lineChart, "count(k='id.keyword')", resultListLine));

    Map<String, String> lineChartFunc = new LinkedHashMap<>();
    lineChartFunc.put("type", "LineChart");
    lineChartFunc.put("function", "count");
    lineChartFunc.put("field", "id.keyword");
    assertTrue(compareResponse(sampleResponse1, lineChartFunc, null, resultListLine));

    String sampleResponse2 =
        "{\"took\":100,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":192,\"relation\":\"eq\"},\"max_score\":null,\"hits\":[]},\"aggregations\":{\"sterms#0\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":[{\"key\":\"Table\",\"doc_count\":85,\"date_histogram#1\":{\"buckets\":[{\"key_as_string\":\"2024-07-18T00:00:00.000Z\",\"key\":1721260800000,\"doc_count\":5,\"filter#filer0\":{\"doc_count\":0,\"value_count#id.keyword0\":{\"value\":0}},\"value_count#id.keyword1\":{\"value\":5}}]}},{\"key\":\"Tag\",\"doc_count\":74,\"date_histogram#1\":{\"buckets\":[{\"key_as_string\":\"2024-07-18T00:00:00.000Z\",\"key\":1721260800000,\"doc_count\":10,\"filter#filer0\":{\"doc_count\":10,\"value_count#id.keyword0\":{\"value\":10}},\"value_count#id.keyword1\":{\"value\":10}}]}},{\"key\":\"StoredProcedure\",\"doc_count\":15,\"date_histogram#1\":{\"buckets\":[{\"key_as_string\":\"2024-07-18T00:00:00.000Z\",\"key\":1721260800000,\"doc_count\":3,\"filter#filer0\":{\"doc_count\":0,\"value_count#id.keyword0\":{\"value\":0}},\"value_count#id.keyword1\":{\"value\":3}}]}},{\"key\":\"Database\",\"doc_count\":9,\"date_histogram#1\":{\"buckets\":[{\"key_as_string\":\"2024-07-18T00:00:00.000Z\",\"key\":1721260800000,\"doc_count\":1,\"filter#filer0\":{\"doc_count\":0,\"value_count#id.keyword0\":{\"value\":0}},\"value_count#id.keyword1\":{\"value\":1}}]}},{\"key\":\"DatabaseSchema\",\"doc_count\":9,\"date_histogram#1\":{\"buckets\":[{\"key_as_string\":\"2024-07-18T00:00:00.000Z\",\"key\":1721260800000,\"doc_count\":1,\"filter#filer0\":{\"doc_count\":0,\"value_count#id.keyword0\":{\"value\":0}},\"value_count#id.keyword1\":{\"value\":1}}]}}]}}}";
    Map<String, String> lineChartFormula = new LinkedHashMap<>();
    lineChartFormula.put("type", "LineChart");
    lineChartFormula.put(
        "formula", "(count(k='id.keyword',q='hasDescription: 1')/count(k='id.keyword'))*100");
    lineChartFormula.put("groupBy", "entityType.keyword");
    List<DataInsightCustomChartResult> resultListLineFormula = new ArrayList<>();
    resultListLineFormula.add(
        new DataInsightCustomChartResult().withCount(0d).withDay(1.721241E12).withGroup("Table"));
    resultListLineFormula.add(
        new DataInsightCustomChartResult().withCount(100d).withDay(1.721241E12).withGroup("Tag"));
    resultListLineFormula.add(
        new DataInsightCustomChartResult()
            .withCount(0d)
            .withDay(1.721241E12)
            .withGroup("StoredProcedure"));
    resultListLineFormula.add(
        new DataInsightCustomChartResult()
            .withCount(0d)
            .withDay(1.721241E12)
            .withGroup("Database"));
    resultListLineFormula.add(
        new DataInsightCustomChartResult()
            .withCount(0d)
            .withDay(1.721241E12)
            .withGroup("DatabaseSchema"));
    assertTrue(
        compareResponse(
            sampleResponse2,
            lineChartFormula,
            "(count(k='id.keyword',q='hasDescription: 1')/count(k='id.keyword'))*100",
            resultListLineFormula));
  }
}
