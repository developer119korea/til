# 애플 자동 로그인 구현시 서버 검증에 관하여

앱에서 이전에 로그인한 이력이 있을때, 자동로그인 기능을 사용하고 싶다면

애플 로그인은, 현재 앱에서 자격 증명 상태 요청할 수 있는 API가 별도로 있다.

해당 API를 통해, 현재 앱에 자격 증명이 살아 있으면 서버측에서는 해당 userID로 자격 증명을 검증 할 수 있도록 구성해야한다.

보통 로그인시 idToken을 사용하여 검증하는데, 구글 로그인과 다르게 애플로그인은 자격 증명 검증시 userID만 반환하기 때문이다.

```objc
- (void) getCredentialStateForUser:(NSString *)userId withRequestId:(uint)requestId
{
#if AUTHENTICATION_SERVICES_AVAILABLE
    if (@available(iOS 13.0, tvOS 13.0, macOS 10.15, *))
    {
        [[self appleIdProvider] getCredentialStateForUserID:userId completion:^(ASAuthorizationAppleIDProviderCredentialState credentialState, NSError * _Nullable error) {
            NSNumber *credentialStateNumber = nil;
            NSDictionary *errorDictionary = nil;

            if (error)
                errorDictionary = [AppleAuthSerializer dictionaryForNSError:error];
            else
                credentialStateNumber = @(credentialState);

            NSDictionary *responseDictionary = [AppleAuthSerializer credentialResponseDictionaryForCredentialState:credentialStateNumber errorDictionary:errorDictionary];
            [self sendNativeMessageForDictionary:responseDictionary forRequestId:requestId];
        }];
    }
    else
    {
        [self sendsCredentialStatusInternalErrorWithCode:-100 andMessage:@"Native AppleAuth is only available from iOS 13.0" forRequestWithId:requestId];
    }
#else
    [self sendsCredentialStatusInternalErrorWithCode:-100 andMessage:@"Native AppleAuth is only available from iOS 13.0" forRequestWithId:requestId];
#endif
}
```
