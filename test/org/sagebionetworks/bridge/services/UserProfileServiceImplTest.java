package org.sagebionetworks.bridge.services;

import javax.annotation.Resource;

import org.junit.*;
import org.junit.runner.RunWith;
import org.sagebionetworks.bridge.TestUserAdminHelper;
import org.sagebionetworks.bridge.models.User;
import org.sagebionetworks.bridge.models.UserProfile;
import org.sagebionetworks.bridge.models.UserSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration("classpath:test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class UserProfileServiceImplTest {
    
    @Resource
    UserProfileServiceImpl service;
    
    @Resource
    TestUserAdminHelper helper;
    
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
    public void canUpdateUserProfile() {
        UserProfile userProfile = helper.getUserProfile(session);
        userProfile.setFirstName("Test");
        userProfile.setLastName("Powers");
        
        User updatedUser = service.updateProfile(session.getUser(), userProfile);
        
        assertEquals("Test", updatedUser.getFirstName());
        assertEquals("Powers", updatedUser.getLastName());
    }
}
