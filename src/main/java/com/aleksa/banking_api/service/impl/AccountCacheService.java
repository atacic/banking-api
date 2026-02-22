package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountCacheService {

    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#p0"),
            @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#p1")
    })
    public void evictTwoAccounts(Long accountId1, Long accountId2) {}
}
