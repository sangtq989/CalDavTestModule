package sang.caldav.demo.main_packet;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;

@Component
public class RestTemplateFunction {

    @Autowired
    RestTemplate restTemplate;

    public String doMethod(String url, HttpEntity httpEntity, HttpMethod method) {
        try {
            ResponseEntity<String> response = restTemplate
                    .exchange(url, method, httpEntity, String.class);
            StringReader sin = new StringReader(response.getBody());
            CalendarBuilder calendarBuilder = new CalendarBuilder();
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw ex;
        } catch (HttpClientErrorException ex) {
            throw ex;
        }
    }
    public <T> ResponseEntity<T> restTemplate(String url, HttpEntity httpEntity, HttpMethod method, Class<T> returnType) throws  Exception {
        try {
            ResponseEntity<T> response = restTemplate
                    .exchange(url, method, httpEntity, returnType);
            return response;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage(), ex);
        }
    }
}
