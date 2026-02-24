package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCacheService {

    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#p0"),
            @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#p1")
    })
    public void evictTwoAccounts(Long accountId1, Long accountId2) {
        log.debug("Accounts cache eviction on: accountId1={} | accountId2={}", accountId1, accountId2);
    }
}
