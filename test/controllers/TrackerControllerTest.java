package controllers;

import static org.junit.Assert.assertEquals;
import static org.sagebionetworks.bridge.TestConstants.TIMEOUT;
import static org.sagebionetworks.bridge.TestConstants.TRACKERS_URL;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.bridge.TestUserAdminHelper;
import org.sagebionetworks.bridge.TestUtils;
import org.sagebionetworks.bridge.models.UserSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import play.libs.WS.Response;

import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration("classpath:test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TrackerControllerTest {

    @Resource
    private TestUserAdminHelper helper;
    
    private UserSession session;

    @Before
    public void before() {
        session = helper.createUser();
    }

    @After
    public void after() {
        helper.deleteUser(session);
    }
    
    @Test
    public void returnsCorrectTrackerType() {
        running(testServer(3333), new TestUtils.FailableRunnable() {
            public void testCode() throws Exception {

                Response response = TestUtils.getURL(session.getSessionToken(), TRACKERS_URL).get().get(TIMEOUT);
                assertEquals("HTTP response OK", 200, response.getStatus());
                
                JsonNode node = response.asJson().get("items").get(0);
                System.out.println(node.toString());
                assertEquals("Type is BloodPressure", "BloodPressure", node.get("type").asText());
            }
        });
    }
    
}