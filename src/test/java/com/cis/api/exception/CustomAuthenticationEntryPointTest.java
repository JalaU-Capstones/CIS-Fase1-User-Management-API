package com.cis.api.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomAuthenticationEntryPointTest {

    private final CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();

    @Test
    void commence_ShouldSetResponseStatusAndWriteBody() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authException = mock(AuthenticationException.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authException.getMessage()).thenReturn("Bad token");
        when(response.getOutputStream()).thenReturn(new StubServletOutputStream(outputStream));

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        
        String responseBody = outputStream.toString();
        assertThat(responseBody).contains("\"status\":401");
        assertThat(responseBody).contains("\"error\":\"Unauthorized\"");
        assertThat(responseBody).contains("\"message\":\"Authentication failed: Bad token\"");
    }

    private static class StubServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final java.io.OutputStream outputStream;

        public StubServletOutputStream(java.io.OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
        }
    }
}
