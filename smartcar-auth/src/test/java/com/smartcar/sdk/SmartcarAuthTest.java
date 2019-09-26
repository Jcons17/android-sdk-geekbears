/**
 * Copyright (c) 2017-present, Smartcar, Inc. All rights reserved.

 * You are hereby granted a limited, non-exclusive, worldwide, royalty-free
 * license to use, copy, modify, and distribute this software in source code or
 * binary form, for the limited purpose of this software's use in connection
 * with the web services and APIs provided by Smartcar.
 *
 * As with any software that integrates with the Smartcar platform, your use of
 * this software is subject to the Smartcar Developer Agreement. This copyright
 * notice shall be included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.smartcar.sdk;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({Helper.class})
@RunWith(PowerMockRunner.class)
public class SmartcarAuthTest {

    @Test
    public void smartcarAuth_authUrlBuilder() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String redirectUriEncoded = "scclient123%3A%2F%2Ftest";
        String[] scope = {"read_odometer", "read_vin"};
        String expectedUri = "https://connect.smartcar.com/oauth/authorize?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUriEncoded +
                "&mode=live&scope=read_odometer%20read_vin";


        SmartcarAuth smartcarAuth = new SmartcarAuth(clientId, redirectUri, scope, null);
        String requestUri = smartcarAuth.new AuthUrlBuilder()
                .build();

        assertEquals(expectedUri, requestUri);
    }

    @Test
    public void smartcarAuth_authUrlBuilderWithSetters() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String redirectUriEncoded = "scclient123%3A%2F%2Ftest";
        String[] scope = {"read_odometer", "read_vin"};
        String vin = "1234567890ABCDEFG";
        String expectedUri = "https://connect.smartcar.com/oauth/authorize?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUriEncoded + 
                "&mode=live&scope=read_odometer%20read_vin" +
                "&approval_prompt=force&make=BMW&state=some%20state" + 
                "&single_select=true" +
                "&single_select_vin=" + vin;

        SmartcarAuth smartcarAuth = new SmartcarAuth(clientId, redirectUri, scope, null);
        String requestUri = smartcarAuth.new AuthUrlBuilder()
                .setApprovalPrompt(true)
                .setMakeBypass("BMW")
                .setState("some state")
                .setSingleSelect(true)
                .setSingleSelectVin(vin)
                .build();

        assertEquals(expectedUri, requestUri);
    }

    @Test
    public void smartcarAuth_launchAuthFlow() {

        // Setup Mocks
        mockStatic(Helper.class);
        Context context = mock(Context.class);

        // Execute Method
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        SmartcarAuth smartcarAuth = new SmartcarAuth(clientId, redirectUri, scope, null);
        String authUrl = smartcarAuth.new AuthUrlBuilder().build();

        smartcarAuth.launchAuthFlow(context);

        // Verify Mocks
        verifyStatic(Helper.class, times(1));
        Helper.startActivity(context, authUrl);

    }

    @Test
    public void smartcarAuth_launchAuthFlow_withAuthUrl() {

        // Setup Mocks
        mockStatic(Helper.class);
        Context context = mock(Context.class);

        // Execute Method
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        SmartcarAuth smartcarAuth = new SmartcarAuth(clientId, redirectUri, scope, null);
        String authUrl = smartcarAuth.new AuthUrlBuilder()
            .setState("foo")
            .setMakeBypass("TESLA")
            .setSingleSelect(false)
            .build();

        smartcarAuth.launchAuthFlow(context, authUrl);

        // Verify Mocks
        verifyStatic(Helper.class, times(1));
        Helper.startActivity(context, authUrl);

    }

    @Test
    public void smartcarAuth_addClickHandler() {

        Context context = mock(Context.class);
        View view = mock(View.class);

        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        SmartcarAuth smartcarAuth = new SmartcarAuth(clientId, redirectUri, scope, null);

        smartcarAuth.addClickHandler(context, view);

        Mockito.verify(view, times(1))
            .setOnClickListener(Mockito.any(View.OnClickListener.class));

    }

    @Test
    public void smartcarAuth_addClickHandler_withAuthUrl() {

        Context context = mock(Context.class);
        View view = mock(View.class);

        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        SmartcarAuth smartcarAuth = new SmartcarAuth(clientId, redirectUri, scope, null);
        String authUrl = smartcarAuth.new AuthUrlBuilder()
            .setState("foo")
            .setMakeBypass("TESLA")
            .setSingleSelect(false)
            .build();

        smartcarAuth.addClickHandler(context, view, authUrl);

        Mockito.verify(view, times(1))
                .setOnClickListener(Mockito.any(View.OnClickListener.class));

    }


    @Test
    public void smartcarAuth_receiveResponse() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";

        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                assertEquals(smartcarResponse.getCode(), "testcode123");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri + "?code=testcode123"));
    }

    @Test
    public void smartcarAuth_receiveResponse_mismatchRedirectUri() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        String wrongRedirectUri = "wrongscheme://test";

        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                throw new AssertionError("Response should not be received.");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(wrongRedirectUri));
    }

    @Test
    public void smartcarAuth_receiveResponse_nullUri() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";

        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                throw new AssertionError("Response should not be received.");
            }
        });

        SmartcarAuth.receiveResponse(null);
    }

    @Test
    public void smartcarAuth_receiveResponse_nullCode() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";

        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                assertEquals(smartcarResponse.getErrorDescription(), "Unable to fetch code. Please try again");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri));
    }

    @Test
    public void smartcarAuth_receiveResponse_accessDenied() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                assertEquals(smartcarResponse.getError(), "access_denied");
                assertEquals(smartcarResponse.getErrorDescription(), "User denied access to the requested scope of permissions.");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri + "?error=access_denied&error_description=User%20denied%20access%20to%20the%20requested%20scope%20of%20permissions."));
    }

    @Test
    public void smartcarAuth_receiveResponse_vehicleIncompatible() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                assertEquals(smartcarResponse.getError(), "vehicle_incompatible");
                assertEquals(smartcarResponse.getErrorDescription(), "The user's vehicle is not compatible.");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri + "?error=vehicle_incompatible&error_description=The%20user%27s%20vehicle%20is%20not%20compatible."));
    }

    @Test
    public void smartcarAuth_receiveResponse_vehicleIncompatibleWithVehicle() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";
        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                VehicleInfo responseVehicle = smartcarResponse.getVehicleInfo();
                assertEquals(smartcarResponse.getError(), "vehicle_incompatible");
                assertEquals(smartcarResponse.getErrorDescription(), "The user's vehicle is not compatible.");
                assertEquals(responseVehicle.getVin(), "1FDKE30G4JHA04964");
                assertEquals(responseVehicle.getMake(), "FORD");
                assertEquals(responseVehicle.getModel(), "E-350");
                assertEquals(responseVehicle.getYear(), new Integer(1988));
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri + "?error=vehicle_incompatible" +
                "&error_description=The%20user%27s%20vehicle%20is%20not%20compatible." +
                "&vin=1FDKE30G4JHA04964&make=FORD&model=E-350&year=1988"));
    }

    @Test
    public void smartcarAuth_receiveResponse_nullCodeWithMessage() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";

        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                assertEquals(smartcarResponse.getErrorDescription(), "Unable to fetch code. Please try again");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri + "?error_description=error"));
    }

    @Test
    public void smartcarAuth_receiveResponse_codeWithState() {
        String clientId = "client123";
        String redirectUri = "scclient123://test";
        String scope = "read_odometer read_vin";

        new SmartcarAuth(clientId, redirectUri, scope, new SmartcarCallback() {
            @Override
            public void handleResponse(SmartcarResponse smartcarResponse) {
                assertEquals(smartcarResponse.getCode(), "testCode");
                assertEquals(smartcarResponse.getState(), "testState");
            }
        });

        SmartcarAuth.receiveResponse(Uri.parse(redirectUri + "?code=testCode&state=testState"));
    }
}
