package io.fundrequest.azrael.worker.contracts.claim;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fundrequest.azrael.worker.contracts.claim.controller.ClaimController;
import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimService;
import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimSignature;
import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimSigningService;
import io.fundrequest.azrael.worker.contracts.claim.sign.SignClaimCommand;
import io.fundrequest.azrael.worker.contracts.claim.sign.SignClaimCommandMother;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class ClaimControllerTest {

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private ClaimSigningService claimSigningService;
    private ClaimService claimService;

    @Before
    public void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        converter.setObjectMapper(objectMapper);
        claimSigningService = mock(ClaimSigningService.class);
        claimService = mock(ClaimService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ClaimController(claimSigningService, claimService))
                                 .setMessageConverters(converter)
                                 .build();
    }

    @Test
    public void testSignClaim() throws Exception {
        SignClaimCommand command = SignClaimCommandMother.aSignClaimCommand();

        ClaimSignature sig = new ClaimSignature();
        sig.setAddress(command.getAddress());
        sig.setSolver(command.getSolver());
        sig.setPlatform(command.getPlatform());
        sig.setPlatformId(command.getPlatformId());
        sig.setR("r");
        sig.setS("s");
        sig.setV(27);
        when(claimSigningService.signClaim(command)).thenReturn(sig);


        this.mockMvc.perform(post("/rest/claims/sign").accept(MediaType.APPLICATION_JSON_UTF8)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                      .content(objectMapper.writeValueAsString(command)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().string(objectMapper.writeValueAsString(sig)));
    }

    @Test
    public void testSubmitTransaction() throws Exception {
        SignClaimCommand command = SignClaimCommandMother.aSignClaimCommand();

        ClaimSignature sig = new ClaimSignature();
        sig.setAddress(command.getAddress());
        sig.setSolver(command.getSolver());
        sig.setPlatform(command.getPlatform());
        sig.setPlatformId(command.getPlatformId());
        sig.setR("r");
        sig.setS("s");
        sig.setV(27);

        final String expectedHash = "txHash";
        when(claimService.receiveApprovedClaim(any(ClaimSignature.class)))
                         .thenReturn(expectedHash);

        ClaimTransaction expected = ClaimTransaction.builder()
                                                    .transactionHash(expectedHash)
                                                    .build();

        this.mockMvc.perform(post("/rest/claims/submit").accept(MediaType.APPLICATION_JSON)
                                                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                        .content(objectMapper.writeValueAsString(command)
                                                                )).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().string(objectMapper.writeValueAsString(expected)));
    }
}