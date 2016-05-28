/*
 * Created by Storm Zhang, Feb 11, 2014.
 */

package com.android.volley.extra;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class GsonRequest<T> extends Request<T> {
	public static final Gson mGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
			new ByteArrayToBase64TypeAdapter()).create();

	// Using Android's base64 libraries. This can be replaced with any base64 library.
	private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
		public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return Base64.decode(json.getAsString(), Base64.NO_WRAP);
		}

		public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
		}
	}
	private final Class<T> mClazz;
	private final Listener<T> mListener;
	private final Map<String, String> mHeaders;

	public GsonRequest(String url, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
		this(Method.GET, url, clazz, null, listener, errorListener);
	}

	public GsonRequest(int method, String url, Class<T> clazz, Map<String, String> headers,
			Listener<T> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		this.mClazz = clazz;
		this.mHeaders = headers;
		this.mListener = listener;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return mHeaders != null ? mHeaders : super.getHeaders();
	}

	@Override
	protected void deliverResponse(T response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			return Response.success(mGson.fromJson(json, mClazz),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JsonSyntaxException e) {
			Log.e("gson", e.getMessage());
			Log.e("gson",e.toString());
			return Response.error(new ParseError(e));
		}
	}
}
