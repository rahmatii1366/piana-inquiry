package ir.piana.boot.inquiry;

import ir.piana.boot.inquiry.common.httpclient.RestClientBeanCreatorConfig;
import ir.piana.boot.inquiry.thirdparty.post.gnaf.GNAFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


/**
 * Unit test for simple App.
 */
@SpringBootTest(classes = {
//        RestClientBeanCreator.Clients.class,
        RestClientBeanCreatorConfig.class,
        GNAFService.class
})
@EnableConfigurationProperties(value = { RestClientBeanCreatorConfig.Clients.class })
@ContextConfiguration
public class AppTest {
    @Autowired
    private GNAFService GNAFService;

//    @Test
    void test() {
//        String authToken = insuranceVehicleThirdPartyService.getAuthToken();
//        Assertions.assertNotNull(authToken);
    }
}
