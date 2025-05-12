package api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Main {
public static void main(String[] args) {
		
		String server_ip=args[0];
		String server_port=args[1];
		String host=args[2];
		String dbName= args[3];
		String db_user = args[4];
		String db_password = args[5];
		JsonObject objIn = new JsonObject().put("server_ip", server_ip)
				.put("server_port",server_port)
				.put("host",host )
				.put("db",dbName )
				.put("user",db_user )
				.put("passwd",db_password )
				;
		DeploymentOptions opts = new DeploymentOptions().setConfig(objIn);
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ApiVerticle(),opts,res->{
			if(res.succeeded()) {
				System.out.println("Handles Routes Deployed");
			}else {
				System.err.println("Deployment failed: "+res.cause());
				res.cause().printStackTrace();
			}
		});
	}
}
