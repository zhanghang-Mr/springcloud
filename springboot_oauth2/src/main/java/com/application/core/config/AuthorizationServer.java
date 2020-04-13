package com.application.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * @author Administrator
 * @version 1.0
 * 授权服务配置
 **/
@Configuration
// @EnableAuthorizationServer 注解继承 AuthorizationServerConfigurerAdapter 来配置Oauth2.0授权服务器
@EnableAuthorizationServer 
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {
	
	@Autowired
	private AuthorizationCodeServices authorizationCodeServices;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	

	//引入客户端信息服务
	@Autowired
	private ClientDetailsService clientDetailsService;
	
	// 引入令牌bean
	@Autowired
	private TokenStore tokenStore;

	/***
	 * 用来配置令牌端点的安全约束
	 */
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security
				.tokenKeyAccess("permitAll()")        //  /oauth/check_token
				.checkTokenAccess("permitAll()")       // /oauth/check_token 公开
				.allowFormAuthenticationForClients();  //允许表单认证，申请令牌
		
	}

	/**
	 * 用来配置客户端详情服务（ClientDetailsService），客户端详情信息在这里进行初始化，
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		//暂时使用内存方式
		clients.inMemory()  //使用in-memory 存储
			.withClient("c1")  //设置客户端id   client_id
			.secret(new BCryptPasswordEncoder().encode("secret"))  // 配置 客户端秘钥
			.resourceIds("res1")             //客户端可以访问的资源列表
			//oauth2.0协议支持的五种认证方式 
			//配置该客户端 client 允许的授权类型
			.authorizedGrantTypes("authorization_code","password","client_creadentials","implicit","refresh_token")
			.scopes("all") //配置 允许的授权范围
			.autoApprove(false)  // false 授权的时候会跳转到授权页面，true 不会跳转，直接发送令牌
			.redirectUris("http://www.baidu.com");  
	}

	/**
	 * 用来配置（token）令牌访问端点和令牌服务(tokenservice)
	 * AuthorizationServerEndpointsConfigurer 通过设定以下属性界定支持的授权类型（Grant Types）:
	 * 		1, authenticationManager: 认证管理器，当选择了资源所有者密码（password）授权类型的时候，请设置这个属性注入一个AuthenticationManager对象
	 * 
	 * 		2，userDetailsService:  如果设置了这个属性的话，那说明你有一个自己的 UserDetailsService 接口的实现，或者可以把这个东西设置到全局域上面去
	 * 								列如（GlobalAuthenticationManagerConfigurer这个配置对象），当你设置了这个之后，那么“refresh_token”即刷新令牌授权
	 * 								类型模式的流程中就会包含一个检查，用来确保这个账号是否仍然有效，
	 * 
	 * 		3，authorizationCodeServices: 这个属性是用来设置授权码服务的，即authorizationCodeServices的实例对象，主要用于“authorization_code”授权码类型模式
	 * 
	 * 		4，implicitGrantService: 这个属性用于设置隐式授权模式，用来管理隐式授权模式的状态
	 * 
	 * 		5，tokenGranter: 当你设置了这个（即tokenGranter接口实现），那么授权将会有自己来王权掌控，并且会忽略点上面的几个属性，这个属性一般是用于扩展用途的，
	 * 						即标准的四种模式都满足不了需求时，才会使用这个 
	 * 
	 * AuthorizationServerEndpointsConfigurer 这个配置对象有一个叫做 pathMapping()的方法用来配置端点URL链接，他有两个参数：
	 * 		1，第一个参数：String类型的，这个端点URL的默认链接
	 * 		2，第二个参数：String类型的，你要进行代替的URL链接
	 * 
	 * 以上的参数都将以“/”字符为开始的字符串，框架默认的URL链接如下列表，可以作为pathMapping()方法的第一个参数：
	 * 	1， /oauth/authorize: 授权端点
	 * 	2. /oauth/token: 令牌端点
	 * 	3. /oauth/confirm_access: 用户授权确认提交端点
	 * 	4. /oauth/error: 授权服务错误信息端点
	 * 	5. /oauth/check_token: 用于资源服务访问的令牌解析端点
	 * 	6. /oauth/token_key:提供公有秘钥的端点，如果使用JWT令牌的话 
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints
				.authenticationManager(authenticationManager)     // 密码模式需要配置的
				.authorizationCodeServices(authorizationCodeServices)  //授权码模式需要配置的
				.tokenServices(tokenServices())  //；令牌管理服务，
				.allowedTokenEndpointRequestMethods(HttpMethod.POST); //允许post方式提交 来访问令牌
	}
	
	/**
	 * 配置令牌访问服务
	 * @return
	 */
	@Bean
	public AuthorizationServerTokenServices tokenServices() {
		DefaultTokenServices service = new  DefaultTokenServices(); 
		service.setClientDetailsService(clientDetailsService); //配置客户端信息服务
		service.setSupportRefreshToken(true);       //配置  是否刷新令牌
		service.setTokenStore(tokenStore);          // 配置 令牌存储策略
		service.setAccessTokenValiditySeconds(7200); // 配置令牌有效期，令牌默认有效期2小时
		service.setRefreshTokenValiditySeconds(259200);// 配置令牌刷新时间，默认有效期3天
		return service;
	}
	
	  //设置授权码模式的授权码如何存取，暂时采用内存方式
	    @Bean
	    public AuthorizationCodeServices authorizationCodeServices() {
	        return new InMemoryAuthorizationCodeServices();
	    }
	
//    @Bean
//    public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
//        return new JdbcAuthorizationCodeServices(dataSource);//设置授权码模式的授权码如何存取
//    }
}
