package com.soda.risk.engine.service.extcalc;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 扩展计算服务
 * 提供指标同比、环比、标准差范围、经纬度距离、时间距离等计算能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtendedCalculateService {

    private final RedisCacheService redisCacheService;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 计算指标同比
     *
     * @param featureId 特征ID
     * @param timestamp 当前时间戳(ms)
     * @param count     当前指标值
     * @param dimension 比较维度: week/day/hour
     * @return 同比变化率(百分比)
     */
    public double comparedWithTheSame(long featureId, long timestamp, double count, String dimension) {
        long start = System.currentTimeMillis();
        try {
            long dimensionTime = transformDimensionTime(dimension);
            long lastTimestamp = timestamp - dimensionTime;
            String key = "feature:" + featureId + ":" + FORMAT.format(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(lastTimestamp), ZoneId.systemDefault()));
            String lastValueStr = redisCacheService.get(key);

            if (lastValueStr == null || lastValueStr.isEmpty()) {
                return 0.0;
            }

            double lastCount = Double.parseDouble(lastValueStr);
            if (lastCount == 0) {
                return count > 0 ? 100.0 : 0.0;
            }

            double rate = ((count - lastCount) / lastCount) * 100.0;
            MonitorFacade.insert("[extcalc]ComparedWithTheSame", System.currentTimeMillis() - start);
            return rate;
        } catch (Exception e) {
            log.error("comparedWithTheSame failed, featureId={}, timestamp={}", featureId, timestamp, e);
            return 0.0;
        }
    }

    /**
     * 计算指标环比
     *
     * @param featureId 特征ID
     * @param timestamp 当前时间戳(ms)
     * @param count     当前指标值
     * @return 环比变化率(百分比)
     */
    public double comparedWithTheRing(long featureId, long timestamp, double count) {
        long start = System.currentTimeMillis();
        try {
            // 获取上一个时间点的数据
            String lastKey = "feature:last:" + featureId;
            String lastValueStr = redisCacheService.get(lastKey);

            if (lastValueStr == null || lastValueStr.isEmpty()) {
                return 0.0;
            }

            double lastCount = Double.parseDouble(lastValueStr);
            if (lastCount == 0) {
                return count > 0 ? 100.0 : 0.0;
            }

            double rate = ((count - lastCount) / lastCount) * 100.0;
            MonitorFacade.insert("[extcalc]ComparedWithTheRing", System.currentTimeMillis() - start);
            return rate;
        } catch (Exception e) {
            log.error("comparedWithTheRing failed, featureId={}", featureId, e);
            return 0.0;
        }
    }

    /**
     * 计算标准差范围
     *
     * @param featureId 特征ID
     * @param timestamp 当前时间戳(ms)
     * @param count     当前指标值
     * @param range     标准差倍数
     * @return 是否在标准差范围内
     */
    public boolean isInStandardDeviationRange(long featureId, long timestamp, double count, double range) {
        try {
            if (range < 0) {
                return false;
            }
            Map<Object, Object> stats = redisCacheService.hGetAll("feature:stats:" + featureId);
            if (stats == null || !stats.containsKey("mean") || !stats.containsKey("stddev")) {
                return false;
            }
            double mean = Double.parseDouble(String.valueOf(stats.get("mean")));
            double stddev = Double.parseDouble(String.valueOf(stats.get("stddev")));
            if (!Double.isFinite(mean) || !Double.isFinite(stddev) || stddev < 0) {
                return false;
            }
            return Math.abs(count - mean) <= stddev * range;
        } catch (Exception e) {
            log.error("isInStandardDeviationRange failed, featureId={}", featureId, e);
            return false;
        }
    }

    /**
     * 计算两点之间的经纬度距离（公里）
     *
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return 距离(公里)
     */
    public double latitudeAndLongitudeDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = radLat1 - radLat2;
        double deltaLng = Math.toRadians(lng1 - lng2);

        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2) +
                        Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(deltaLng / 2), 2)));
        s = s * 6378.137; // 地球半径
        s = Math.round(s * 10000.0) / 10000.0;
        return s;
    }

    /**
     * 计算时间距离（秒）
     *
     * @param timestamp1 时间戳1(ms)
     * @param timestamp2 时间戳2(ms)
     * @return 时间差(秒)
     */
    public long dataTimeDistance(long timestamp1, long timestamp2) {
        return Math.abs(timestamp1 - timestamp2) / 1000;
    }

    private long transformDimensionTime(String dimension) {
        if (dimension == null) {
            return 86400000L; // 默认1天
        }
        switch (dimension.toLowerCase()) {
            case "hour":
                return 3600000L;
            case "day":
                return 86400000L;
            case "week":
                return 604800000L;
            default:
                return 86400000L;
        }
    }
}
