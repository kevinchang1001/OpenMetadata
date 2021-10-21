package org.openmetadata.catalog.jdbi3;

import org.openmetadata.catalog.jdbi3.UsageRepository.UsageDetailsMapper;
import org.openmetadata.catalog.type.UsageDetails;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(UsageDetailsMapper.class)
public interface UsageDAO {
  @SqlUpdate("INSERT INTO entity_usage (usageDate, id, entityType, count1, count7, count30) " +
          "SELECT :date, :id, :entityType, :count1, " +
          "(:count1 + (SELECT COALESCE(SUM(count1), 0) FROM entity_usage WHERE id = :id AND usageDate >= :date - " +
          "INTERVAL 6 DAY)), " +
          "(:count1 + (SELECT COALESCE(SUM(count1), 0) FROM entity_usage WHERE id = :id AND usageDate >= :date - " +
          "INTERVAL 29 DAY))")
  void insert(@Bind("date") String date, @Bind("id") String id, @Bind("entityType") String entityType, @Bind(
          "count1") int count1);

  @SqlUpdate("INSERT INTO entity_usage (usageDate, id, entityType, count1, count7, count30) " +
          "SELECT :date, :id, :entityType, :count1, " +
          "(:count1 + (SELECT COALESCE(SUM(count1), 0) FROM entity_usage WHERE id = :id AND usageDate >= :date - " +
          "INTERVAL 6 DAY)), " +
          "(:count1 + (SELECT COALESCE(SUM(count1), 0) FROM entity_usage WHERE id = :id AND usageDate >= :date - " +
          "INTERVAL 29 DAY)) " +
          "ON DUPLICATE KEY UPDATE count1 = count1 + :count1, count7 = count7 + :count1, count30 = count30 + :count1")
  void insertOrUpdateCount(@Bind("date") String date, @Bind("id") String id, @Bind("entityType") String entityType,
                           @Bind("count1") int count1);

  @SqlUpdate("UPDATE entity_usage u JOIN ( " +
          "SELECT u1.id, " +
          "(SELECT COUNT(*) FROM entity_usage as u2 WHERE u2.count1 <  u1.count1 AND u2.entityType = :entityType " +
          "AND u2.usageDate = :date) as p1, " +
          "(SELECT COUNT(*) FROM entity_usage as u3 WHERE u3.count7 <  u1.count7 AND u3.entityType = :entityType " +
          "AND u3.usageDate = :date) as p7, " +
          "(SELECT COUNT(*) FROM entity_usage as u4 WHERE u4.count30 <  u1.count30 AND u4.entityType = :entityType " +
          "AND u4.usageDate = :date) as p30, " +
          "(SELECT COUNT(*) FROM entity_usage WHERE entityType = :entityType AND usageDate = :date) as total " +
          "FROM entity_usage u1 WHERE u1.entityType = :entityType AND u1.usageDate = :date" +
          ") vals ON u.id = vals.id AND usageDate = :date " +
          "SET u.percentile1 = ROUND(100 * p1/total, 2), u.percentile7 = ROUND(p7 * 100/total, 2), u.percentile30 =" +
          " ROUND(p30*100/total, 2)")
  @SqlQuery("SELECT id, usageDate, entityType, count1, count7, count30, " +
          "percentile1, percentile7, percentile30 FROM entity_usage " +
          "WHERE id = :id AND usageDate >= :date - INTERVAL :days DAY AND usageDate <= :date ORDER BY usageDate DESC")
  List<UsageDetails> getUsageById(@Bind("id") String id, @Bind("date") String date, @Bind("days") int days);

  /**
   * Get latest usage record
   **/
  @SqlQuery("SELECT id, usageDate, entityType, count1, count7, count30, " +
          "percentile1, percentile7, percentile30 FROM entity_usage " +
          "WHERE usageDate IN (SELECT MAX(usageDate) FROM entity_usage WHERE id = :id) AND id = :id")
  UsageDetails getLatestUsage(@Bind("id") String id);

  @SqlUpdate("DELETE FROM entity_usage WHERE id = :id")
  int delete(@Bind("id") String id);

  /**
   * Note not using in following percentile computation PERCENT_RANK function as unit tests use mysql5.7 and it does
   * not have window function
   */
  @SqlUpdate("UPDATE entity_usage u JOIN ( " +
          "SELECT u1.id, " +
          "(SELECT COUNT(*) FROM entity_usage as u2 WHERE u2.count1 <  u1.count1 AND u2.entityType = :entityType " +
          "AND u2.usageDate = :date) as p1, " +
          "(SELECT COUNT(*) FROM entity_usage as u3 WHERE u3.count7 <  u1.count7 AND u3.entityType = :entityType " +
          "AND u3.usageDate = :date) as p7, " +
          "(SELECT COUNT(*) FROM entity_usage as u4 WHERE u4.count30 <  u1.count30 AND u4.entityType = :entityType " +
          "AND u4.usageDate = :date) as p30, " +
          "(SELECT COUNT(*) FROM entity_usage WHERE entityType = :entityType AND usageDate = :date) as total " +
          "FROM entity_usage u1 WHERE u1.entityType = :entityType AND u1.usageDate = :date" +
          ") vals ON u.id = vals.id AND usageDate = :date " +
          "SET u.percentile1 = ROUND(100 * p1/total, 2), u.percentile7 = ROUND(p7 * 100/total, 2), u.percentile30 =" +
          " ROUND(p30*100/total, 2)")
  void computePercentile(@Bind("entityType") String entityType, @Bind("date") String date);

}
