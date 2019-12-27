/*
 * Copyright (c) 2015-2020, Chen Rui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vianet.azure.sdk.manage.support.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.vianet.azure.sdk.manage.support.cache.Cache;
import com.vianet.azure.sdk.manage.support.cache.CacheManager;
import com.vianet.azure.sdk.manage.support.cache.NullCacheManage;
import com.vianet.azure.sdk.manage.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties文件载入工具类. 可载入多个properties文件,
 * 相同的属性在最后载入的文件中的值将会覆盖之前的值，但以System的Property优先.
 */
public class PropertiesLoader {

	private static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

	private final static String CACHE_KEY = "SYSTEM_PROPERTIES";

	private CacheManager cacheManager = new NullCacheManage();

	private Cache cache = cacheManager.getCache(CACHE_KEY);

	public PropertiesLoader(String... resourcesPaths) {
		this.loadProperties(resourcesPaths);
	}

	/**
	 * 取出Property。
	 */
	private String getValue(String key) {
		String systemProperty = System.getProperty(key);
		if (systemProperty != null) {
			return systemProperty;
		}
		return cache.get(key).toString();
	}

	/**
	 * 取出String类型的Property,如果都為Null则抛出异常.
	 */
	public String getProperty(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return value;
	}

	/**
	 * 取出String类型的Property.如果都為Null則返回Default值.
	 */
	public String getProperty(String key, String defaultValue) {
		String value = getValue(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * 取出Integer类型的Property.如果都為Null或内容错误则抛出异常.
	 */
	public Integer getInteger(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Integer.valueOf(value);
	}

	/**
	 * 取出Integer类型的Property.如果都為Null則返回Default值，如果内容错误则抛出异常
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		String value = getValue(key);
		return value != null ? Integer.valueOf(value) : defaultValue;
	}

	/**
	 * 取出Double类型的Property.如果都為Null或内容错误则抛出异常.
	 */
	public Double getDouble(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Double.valueOf(value);
	}

	/**
	 * 取出Double类型的Property.如果都為Null則返回Default值，如果内容错误则抛出异常
	 */
	public Double getDouble(String key, Integer defaultValue) {
		String value = getValue(key);
		return value != null ? Double.valueOf(value) : defaultValue;
	}

	/**
	 * 取出Boolean类型的Property.如果都為Null抛出异常,如果内容不是true/false则返回false.
	 */
	public Boolean getBoolean(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Boolean.valueOf(value);
	}

	/**
	 * 取出Boolean类型的Propert.如果都為Null則返回Default值,如果内容不为true/false则返回false.
	 */
	public Boolean getBoolean(String key, boolean defaultValue) {
		String value = getValue(key);
		return value != null ? Boolean.valueOf(value) : defaultValue;
	}

	/**
	 * 载入多个文件, 文件路径使用Spring Resource格式.
	 */
	private void loadProperties(String... resourcesPaths) {
		for (String location : resourcesPaths) {
			logger.debug("Loading properties file from path:{}", location);
			if (cache.get(location) != null) {
				continue;
			}
			InputStream is = null;
			try {
				is = this.getClass().getResourceAsStream(location);
				Properties props = new Properties();
				props.load(is);
				for (Object key : props.keySet()) {
					cache.put(key, props.get(key));
				}
				cache.put(location, true);
			} catch (IOException ex) {
				logger.info("Could not load properties from path:{}, {} ", location, ex.getMessage());
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}