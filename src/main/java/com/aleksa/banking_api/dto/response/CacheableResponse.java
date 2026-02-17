package com.aleksa.banking_api.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface CacheableResponse extends Serializable {
}
