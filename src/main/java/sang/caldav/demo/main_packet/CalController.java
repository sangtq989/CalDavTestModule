package sang.caldav.demo.main_packet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class CalController {
    @Autowired
    CalService calService;

    @GetMapping(value = "/{userName}/{password}")
    public ResponseEntity getData(@PathVariable(value = "userName") String username,
                                  @PathVariable(value = "password") String password) throws Exception {
        return calService.getData(username, password);
    }

    @GetMapping(value = "ok/{userName}/{password}")
    public ResponseEntity getDataOkHTTP(@PathVariable(value = "userName") String username,
                                        @PathVariable(value = "password") String password) throws Exception {
        return calService.getDataOkHttp(username, password);
    }

    @GetMapping(value = "/convert/{token}")
    public String convert(@PathVariable String token){
        String username = new String(Base64.getDecoder().decode(token));
        return username.substring(0, username.indexOf(":"));
    }
}
