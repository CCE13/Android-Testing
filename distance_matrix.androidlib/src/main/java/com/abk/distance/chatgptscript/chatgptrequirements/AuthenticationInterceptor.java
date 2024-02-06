package com.abk.distance.chatgptscript.chatgptrequirements;

/**
 * OkHttp Interceptor that adds an authorization token header
 * 
 * @deprecated Use {@link com.theokanning.openai.client.AuthenticationInterceptor}
 */
@Deprecated
public class AuthenticationInterceptor extends com.theokanning.openai.client.AuthenticationInterceptor {

    AuthenticationInterceptor(String token) {
        super(token);
    }

}
