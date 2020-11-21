package sang.caldav.demo.main_packet;

import com.github.caldav4j.exceptions.CalDAV4JException;
import org.apache.jackrabbit.webdav.DavException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
interface CalService {
    ResponseEntity<String> getData(String username, String password) throws IOException;

    ResponseEntity getDataOkHttp(String username, String password) throws IOException, DavException, CalDAV4JException;

    ResponseEntity testing(String username, String password) throws Exception;

    ResponseEntity convertTest(String token);


}
