package io.fundrequest.azrael.worker.contracts.refund.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fundrequest.azrael.worker.contracts.refund.RefundTransaction;
import io.fundrequest.azrael.worker.contracts.refund.service.RefundService;
import io.fundrequest.azrael.worker.contracts.refund.sign.RefundRequest;
import io.fundrequest.azrael.worker.contracts.refund.sign.RefundRequestMother;
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

public class RefundControllerTest {

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private RefundService refundService;

    @Before
    public void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        converter.setObjectMapper(objectMapper);
        refundService = mock(RefundService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RefundController(refundService))
                                 .setMessageConverters(converter)
                                 .build();
    }

    @Test
    public void submitTransaction() throws Exception {
        final RefundRequest command = RefundRequestMother.aRefundRequest();

        final String expectedHash = "txHash";
        when(refundService.submit(any(RefundRequest.class)))
                .thenReturn(expectedHash);

        final RefundTransaction expected = RefundTransaction.builder()
                                                            .transactionHash(expectedHash)
                                                            .build();

        this.mockMvc.perform(post("/rest/refund/submit").accept(MediaType.APPLICATION_JSON)
                                                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                        .content(objectMapper.writeValueAsString(command)
                                                                )).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().string(objectMapper.writeValueAsString(expected)));
    }
}