package sang.caldav.demo.main_packet;


import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.CalDAVConstants;
import com.github.caldav4j.exceptions.CalDAV4JException;
import com.github.caldav4j.methods.CalDAV4JMethodFactory;
import com.github.caldav4j.methods.HttpPropFindMethod;
import com.github.caldav4j.model.request.*;
import com.github.caldav4j.util.GenerateQuery;
import com.github.caldav4j.util.UrlUtils;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.Date;

@Service
public class CalServiceImpl implements CalService {

    @Autowired
    RestTemplateFunction restTemplateFunction;

    public static String nullOrValue(Property o) {
        if (Objects.isNull(o)) {
            return null;
        }
        return o.getValue();
    }



    @Override
    public ResponseEntity getDataOkHttp(String username, String password) throws CalDAV4JException {

        Header[] headers = {
                new BasicHeader("Content-Type", "application/xml"),
                new BasicHeader("Authorization", "Basic c2FuZ3BkQGhpdmV0ZWNoLm9ubGluZToxZTNxMndhZFM=")
        };

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultHeaders(Arrays.asList(headers))
                .build();
        CalDAVCollection calDAVCollection =
                new CalDAVCollection(
                        "https://mail.hivetech.vn:2080/calendars/sangpd@hivetech.online/calendar:f6ef57d1-7965-a01d-960d-7fc47ca4c255/"
                );

        GenerateQuery gq = new GenerateQuery();
        CalendarMultiget query = new CalendarMultiget();
        CalendarData calendarData = new CalendarData();
        query.addProperty(CalDAVConstants.DNAME_GETETAG);
        query.setCalendarDataProp(calendarData);

        CalendarQuery calendarQuery = gq.generate();
        List<Calendar> calendars = calDAVCollection.queryCalendars(httpclient, calendarQuery);

        List<CalendarSaved> calendarsSaved = new ArrayList<>();
//        calendarsSaved.add(new CalendarSaved("event1", "An updated Event", new Date()));

        calendars.forEach(item -> {
            ComponentList componentList = item.getComponents().getComponents(Component.VEVENT);
            Iterator<VEvent> eventIterator = componentList.iterator();
            while (eventIterator.hasNext()) {

                CalendarSaved eventItem = new CalendarSaved();
                VEvent ve = eventIterator.next();
                eventItem.id = ve.getUid().getValue();
                eventItem.eventName = ve.getSummary().getValue();
                eventItem.lastModified = ve.getLastModified().getDate();
                eventItem.rule = ve.getProperty("RRULE");
                calendarsSaved.add(eventItem);
            }
        });

        return new ResponseEntity(calendarsSaved, org.springframework.http.HttpStatus.OK);
    }

    @Override
    public ResponseEntity testing(String username, String password) {
        String url = "https://webmail.hivetech.vn:2096/execute/DAV/get_calendar_contacts_config";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("user", username);
        body.add("pass", password);
        body.add("goto_uri", "/execute/DAV/get_calendar_contacts_config");
        final org.springframework.http.HttpEntity httpEntity = new org.springframework.http.HttpEntity(headers);

        try {
            String calendar = restTemplateFunction.doMethod(url, httpEntity, HttpMethod.GET);
            return new ResponseEntity(calendar, HttpStatus.OK);
        } catch (HttpClientErrorException.Unauthorized ex) {
            return new ResponseEntity("Unauthorzie", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity convertTest(String token) {
        return null;
    }

    private void print_Xml(XmlSerializable ace) throws TransformerException, ParserConfigurationException {
        Document document = DomUtil.createDocument();
        ElementoString(ace.toXml(document));
    }

    private String ElementoString(Element node) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(node);
        transformer.transform(source, result);

        String xmlString = result.getWriter().toString();
        return xmlString;
    }

    @Override
    public ResponseEntity<String> getData(String username, String password) throws IOException {
        String authHeader = username + ":" + password;
        String token = Base64.getEncoder().encodeToString(authHeader.getBytes());
        String url = "https://mail.hivetech.online:2080/calendars/sangpd@hivetech.online/";

        Header[] headers = {
                new BasicHeader("Content-type", "Application/xml"),
                new BasicHeader("Authorization", "Basic c2FuZ3BkQGhpdmV0ZWNoLm9ubGluZToxZTNxMndhZFM=")
        };

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultHeaders(Arrays.asList(headers))
                .build();


        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(DavPropertyName.create(CalDAVConstants.DAV_DISPLAYNAME));
        set.add(CalDAVConstants.DNAME_CALENDAR_DESCRIPTION);

        CalDAV4JMethodFactory methodFactory = new CalDAV4JMethodFactory();
        set.getContent();
        HttpPropFindMethod method = methodFactory.createPropFindMethod(
                url,
                set,
                CalDAVConstants.DEPTH_1);
        try {
            HttpResponse response = httpclient.execute(method);
            List<Map<String, String>> calendarList = calFunction(method.getResponseBodyAsMultiStatus(response).getResponses());

            return new ResponseEntity(calendarList, HttpStatus.OK);
        } catch (IOException | DavException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("No", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity login(String username, String password) throws Exception {
        String url = "https://webmail.hivetech.vn:2096/login";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("user", username);
        body.add("pass", password);
        body.add("goto_uri", "/execute/DAV/get_calendar_contacts_config");
        final HttpEntity httpEntity = new HttpEntity(body, headers);
        try {
            ResponseEntity<String> webMailReturn = restTemplateFunction.restTemplate(url, httpEntity, HttpMethod.POST, String.class);
            String prevCookie = webMailReturn.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE);

            /* Because webmail login doesn't has login api, they return a html with HTML-EQUIV="refresh" to redirect
             *  So we need to extract the url with session token to continue the request
             *  Maybe need to review this
             * */
            String urlRedirect = StringUtils.substringBetween(webMailReturn.getBody(), "URL=", "\"></head>");
            org.springframework.http.HttpHeaders headers2 = new org.springframework.http.HttpHeaders();
            headers2.set("Cookie", prevCookie);
            headers2.set("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7");

            ResponseEntity<String> result = restTemplateFunction.restTemplate("https://webmail.hivetech.vn:2096/" + urlRedirect,
                    new HttpEntity(headers2),
                    HttpMethod.GET,
                    String.class);
            return new ResponseEntity(result.getBody(), HttpStatus.OK);
        } catch (Exception ex) {
            throw new Exception("Unauthorize" + ex);
        }
    }

    private class CalendarSaved {
        String id;
        String eventName;
        Date lastModified;
        RRule rule;

        public CalendarSaved(String event1, String eventName, Date date, RRule rRule) {
            this.id = event1;
            this.eventName = eventName;
            this.lastModified = date;

        }

        public CalendarSaved() {

        }
    }

    private List<Map<String, String>> calFunction(MultiStatusResponse[] responses) {
        List<Map<String, String>> calendarResponse = new ArrayList<>();
        try {
            if (responses != null) {
                MultiStatusResponse[] var5 = responses;
                int length = responses.length;
                for (int i = 0; i < length; ++i) {
                    MultiStatusResponse r = var5[i];
                    DavPropertySet props = r.getProperties(200);
                    String displayName = "";
                    String desc = "";
                    if (Objects.nonNull(props.get(DavPropertyName.create(CalDAVConstants.DAV_DISPLAYNAME)))) {
                        displayName = props.get(DavPropertyName.create(CalDAVConstants.DAV_DISPLAYNAME)).getValue().toString();
                    }
                    if (Objects.nonNull(props.get(CalDAVConstants.DNAME_CALENDAR_DESCRIPTION))) {
                        desc = props.get(CalDAVConstants.DNAME_CALENDAR_DESCRIPTION).getValue().toString();
                    }
                    String finalDisplayName = displayName;
                    if (!"".equals(finalDisplayName)) {
                        String finalDesc = desc;
                        calendarResponse.add(
                                new HashMap() {{
                                    put("id", r.getHref());
                                    put("name", finalDisplayName);
                                    put("description", finalDesc);
                                }}
                        );
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendarResponse;
    }
}
