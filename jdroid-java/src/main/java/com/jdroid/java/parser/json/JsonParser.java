package com.jdroid.java.parser.json;

import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import com.jdroid.java.collections.Lists;
import com.jdroid.java.json.JsonArrayWrapper;
import com.jdroid.java.json.JsonObjectWrapper;
import com.jdroid.java.parser.Parser;
import com.jdroid.java.utils.FileUtils;
import com.jdroid.java.utils.LoggerUtils;
import com.jdroid.java.utils.StringUtils;

/**
 * JSON input streams parser
 * 
 * @param <T>
 */
public abstract class JsonParser<T> implements Parser {
	
	private static final Logger LOGGER = LoggerUtils.getLogger(JsonParser.class);
	private static final String ARRAY_PREFIX = "[";
	
	/**
	 * @see com.jdroid.java.parser.Parser#parse(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(String input) {
		
		LOGGER.debug("Parsing started.");
		try {
			LOGGER.trace(input);
			
			// Create a wrapped JsonObjectWrapper or JsonArrayWrapper
			T json = null;
			if (input.startsWith(ARRAY_PREFIX)) {
				json = (T)new JsonArrayWrapper(input);
			} else {
				json = (T)new JsonObjectWrapper(input);
			}
			
			// Parse the JSONObject
			return parse(json);
		} finally {
			LOGGER.debug("Parsing finished.");
		}
	}
	
	/**
	 * @see com.jdroid.java.parser.Parser#parse(java.io.InputStream)
	 */
	@Override
	public Object parse(InputStream inputStream) {
		String content = FileUtils.toString(inputStream);
		return StringUtils.isNotBlank(content) ? parse(content) : null;
	}
	
	/**
	 * @param json
	 * @return The parsed object
	 */
	public abstract Object parse(T json);
	
	/**
	 * Parses a list of items.
	 * 
	 * @param <ITEM> The item's type.
	 * 
	 * @param jsonObject The {@link JsonObjectWrapper} to parse.
	 * @param jsonKey The key for the Json array.
	 * @param parser The {@link JsonParser} to parse each list item.
	 * @return The parsed list.
	 */
	protected <ITEM> List<ITEM> parseList(JsonObjectWrapper jsonObject, String jsonKey,
			JsonParser<JsonObjectWrapper> parser) {
		return parseList(jsonObject.getJSONArray(jsonKey), parser);
	}
	
	/**
	 * Parses a list of items.
	 * 
	 * @param <ITEM> The item's type.
	 * 
	 * @param json The {@link JsonArrayWrapper} to parse.
	 * @param parser The {@link JsonParser} to parse each list item.
	 * @return The parsed list.
	 */
	@SuppressWarnings("unchecked")
	protected <ITEM> List<ITEM> parseList(JsonArrayWrapper jsonArray, JsonParser<JsonObjectWrapper> parser) {
		List<ITEM> list = Lists.newArrayList();
		if (jsonArray != null) {
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				list.add((ITEM)parser.parse(jsonArray.getJSONObject(i)));
			}
		}
		return list;
	}
	
	protected List<String> parseListString(JsonArrayWrapper jsonArray) {
		List<String> list = Lists.newArrayList();
		if (jsonArray != null) {
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				list.add(jsonArray.getString(i));
			}
		}
		return list;
	}
	
	protected List<Long> parseListLong(JsonArrayWrapper jsonArray) {
		List<Long> list = Lists.newArrayList();
		if (jsonArray != null) {
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				list.add(jsonArray.getLong(i));
			}
		}
		return list;
	}
}
