package api;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
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
	
	private void handleQuillInsert(RoutingContext ctx) {
		@SuppressWarnings("depreciation")
		JsonObject body = ctx.getBodyAsJson();
		String insert = body.getString("insert");
		System.out.println("insert"+insert);
		
		ctx.response().setStatusCode(201)
		.end(new JsonObject().put("Message", "Recieved").encode());
	}
	
	
}
