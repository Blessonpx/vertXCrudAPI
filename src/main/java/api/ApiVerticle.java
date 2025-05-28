package api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;

public class ApiVerticle extends AbstractVerticle{
	
	@SuppressWarnings("deprecation")
	private MySQLPool client;
	
		
	@Override
	public void start(Promise<Void> startPromise) {
		/*
		 * Adding headers for Cors
		 * 
		 * */
		Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("x-requested-with");
		allowedHeaders.add("Access-Control-Allow-Origin");
		allowedHeaders.add("origin");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("accept");
		allowedHeaders.add("Authorization");
		
		Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		
		
		
		MySQLConnectOptions connect = new MySQLConnectOptions()
				.setPort(3306)
				.setHost(config().getString("host"))
				.setDatabase(config().getString("db"))
				.setUser(config().getString("user"))
				.setPassword(config().getString("passwd"));
	
		PoolOptions poolOpts = new PoolOptions().setMaxSize(10);
	    client = MySQLPool.pool(vertx, connect, poolOpts);
	    
	    
	    
	    Router router = Router.router(vertx);
	    router.route().handler(BodyHandler.create());      // JSON body to Buffer
	    router.route().handler(CorsHandler.create("*")
	    		.allowedHeaders(allowedHeaders)
	    		.allowedMethods(allowedMethods));
	    // POST /blog  ─ create
	    router.post("/createLog").handler(this::handleCreate);
	    router.post("/quill/create").handler(this::handleQuillInsert);
	    router.get("/eg/getRepos").handler(this::egGetRepos);
	    
	    /*
	     * Declare Gitlab Repos Separately
	     * 
	     * 
	     * */
	    

	    router.get("/git/getRepos").handler(ctx->{
	    	vertx.executeBlocking(promise ->{
	    		try {
	    			
	    			String gitURL=config().getString("gitURL");
	    			String token=config().getString("token");
	    			GitLabApi gitLabApi = new GitLabApi(gitURL, token);
	                List<Project> projects = gitLabApi.getProjectApi().getProjects();
	                promise.complete(projects);
	                
//	    			String gitURL=config().getString("gitURL");
//	    			String token=config().getString("token");
//	    			GitLabApi gitLabApi = new GitLabApi(gitURL, token);
//	                List<Project> projects = gitLabApi.getProjectApi().getProjects();
//	                
//	                /*filter only the once with user*/
//	                List<Project> filteredProjects = projects.stream()
//	                        .filter(project -> project.getPathWithNamespace().contains("/cart.user/"))
//	                        .toList();

	                //promise.complete(filteredProjects);
	                promise.complete(projects);
	    		}catch(Exception e) {
	    			promise.fail(e);
	    		}
	    	},res->{
	    		if (res.succeeded()) {
	    			List<Project> filteredProjects = (List<Project>) res.result();
	                JsonArray result = new JsonArray();
	                for (Project p : filteredProjects) {
	                	if (p.getWebUrl().contains("user")) {
	                    result.add(new JsonObject()
	                        .put("id", p.getId())
	                        .put("name", p.getName())
	                        .put("web_url", p.getWebUrl()));
	                	}
	                }
	                ctx.response()
	                   .putHeader("Content-Type", "application/json")
	                   .end(result.encodePrettily());
	    		}
	    		else {
	    			ctx.response()
	                .setStatusCode(500)
	                .putHeader("Content-Type", "application/json")
	                .end(new JsonObject()
	                     .put("error", res.cause().getMessage())
	                     .encode());
	    		}
	    	}
	    			);
	    });
	    
	    
	    
	    
	    
	    
	    
	    
	 	    	    
	    vertx.createHttpServer()
        .requestHandler(router)
        .listen(Integer.parseInt(config().getString("server_port")),config().getString("server_ip")
        		, res -> {
          if (res.succeeded()) {
            System.out.println("✔  CRUD API listening ...");
            startPromise.complete();
          } else {
            startPromise.fail(res.cause());
          }
        });
	}
		
	
	private void handleCreate(RoutingContext ctx) {
		@SuppressWarnings("deprecation")
		JsonObject body = ctx.getBodyAsJson();
	    String id  = body.getString("id");
	    String content = body.getString("content");
	    
	    client.preparedQuery("INSERT INTO BLOG_LOG (id,blog_content) VALUES (?,?)")
	    .execute(Tuple.of(id,content),ar->{
	    	if (ar.succeeded()) {
	    		ctx.response().setStatusCode(201)
                .end(new JsonObject().put("id", id).encode());
	    	}else {
	    		ctx.fail(ar.cause());
	    	}
	    });
	    
	}
	
	private void egGetRepos(RoutingContext ctx) {
		JsonArray repos = new JsonArray()
	            .add(new JsonObject()
	                .put("id", 153)
	                .put("name", "jenkins_test_1")
	                .put("web_url", "http://port.user/jenkins_test_1"))
	            .add(new JsonObject()
	                .put("id", 149)
	                .put("name", "diyui")
	                .put("web_url", "http://port.user/diyui"))
	            .add(new JsonObject()
	            	.put("id", 148)
	            	.put("name", "customer360")
	            	.put("web_url", "http://port.user/customer360"))
	            .add(new JsonObject()
	            	.put("id", 146)
	            	.put("name", "diy_campaign_ui_sso")
	            	.put("web_url", "http://port.user/diy_campaign_ui_sso"))
	            .add(new JsonObject()
	            	.put("id", 145)
	            	.put("name", "ui_code_keycloak")
	            	.put("web_url", "http://port.user/ui_code_keycloak"))
	            .add(new JsonObject()
	            	.put("id", 144)
	            	.put("name", "sso")
	            	.put("web_url", "http://port.user/sso"))
	            .add(new JsonObject()
	            	.put("id", 142)
	            	.put("name", "adapter")
	            	.put("web_url", "http://port.user/adapter"))
	            .add(new JsonObject()
	            	.put("id", 141)
	            	.put("name", "react_solus_user")
	            	.put("web_url", "http://port.user/react_solus_user"))         
	            ;

	        ctx.response()
	               .putHeader("Content-Type", "application/json")
	               .end(repos.encodePrettily());
	}
	
	private void handleQuillInsert(RoutingContext ctx) {
		@SuppressWarnings("depreciation")
		JsonObject body = ctx.getBodyAsJson();
		String insert = body.getString("insert");
		System.out.println("insert"+insert);
		
		ctx.response().setStatusCode(201)
		.end(new JsonObject().put("Message", "Recieved").encode());
	}
	
	
}
