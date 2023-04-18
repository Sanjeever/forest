package org.dromara.forest.springboot3.test.address;

import org.dromara.forest.annotation.Address;
import org.dromara.forest.annotation.BaseRequest;
import org.dromara.forest.annotation.Get;
import org.dromara.forest.annotation.Var;
import org.dromara.forest.http.ForestRequest;

@BaseRequest(baseURL = "http://localhost")
@Address(host = "127.0.0.1", port = "${port}")
public interface SpringBaseAddressClient {

    @Get("/")
    ForestRequest<String> testBaseAddress(@Var("port") int port);

}