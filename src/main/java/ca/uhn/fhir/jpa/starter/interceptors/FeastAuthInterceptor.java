package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.starter.components.DummyUserIdentification;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import org.hl7.fhir.instance.model.api.IIdType;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.jpa.starter.feastutil.oauthCaller;

public class FeastAuthInterceptor extends AuthorizationInterceptor{
    
    public Logger ourLogger = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

      // Process authorization header - The following is a fake
      // implementation. Obviously we'd want something more real
      // for a production scenario.
      //
      // In this basic example we have two hardcoded bearer tokens,
      // one which is for a user that has access to one patient, and
      // another that has full access.
      ourLogger.info("---> Testing logger on interceptor <---");

      IIdType userIdPatientId = null;
      boolean userIsAdmin = false;
      String authHeader = theRequestDetails.getHeader("Authorization");
      if (authHeader == null) {
         ourLogger.info("===> No Authentication header found, request rejected <===");
         return new RuleBuilder().denyAll().build();
      }
      String token = authHeader.substring("Bearer ".length());
      ourLogger.info("VVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
      ourLogger.info("\tRequest details: ", theRequestDetails.toString());
      ourLogger.info("\t Auth header? " + authHeader);
      ourLogger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

      // if ("Bearer dfw98h38r".equals(authHeader)) {
      //    // This user has access only to Patient/1 resources
      //    userIdPatientId = new DummyUserIdentification("Patient", 1L);
      // } else if ("Bearer 39ff939jgg".equals(authHeader)) {
      //    // This user has access to everything
      //    userIsAdmin = true;
      // } else {
      //    // Throw an HTTP 401
      //   //  throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value");
      //   ourLogger.info("===> What sort of thing are we doing here?! <===");
      //   ourLogger.info("===> Auth header was: ", authHeader);
      //   throw new AuthenticationException(Msg.code(200) + "- Welcome HAPI user. Please provide credentials!!\n");
      // }

      boolean verified = false;
      oauthCaller verifier = new oauthCaller();
      try {
         verified = verifier.verifyToken(token);
      } catch (Exception e) {
         ourLogger.info("EXCEPTION ===> " + e.toString());
      }
      ourLogger.info("Verified? " + verified);
      // If the user is a specific patient, we create the following rule chain:
      // Allow the user to read anything in their own patient compartment
      // Allow the user to write anything in their own patient compartment
      // If a client request doesn't pass either of the above, deny it
      // if (verified) {
      //    return new RuleBuilder()
      //          .allow()
      //          .read()
      //          .allResources()
      //          .inCompartment("Patient", userIdPatientId)
      //          .andThen()
      //          .allow()
      //          .write()
      //          .allResources()
      //          .inCompartment("Patient", userIdPatientId)
      //          .andThen()
      //          .denyAll()
      //          .build();
      // }

      // If the user is a authenticated, allow read access only
      if (verified) {
         return new RuleBuilder()
            .allow()
            .read()
            .allResources()
            .withAnyId()
            .build();
      }

      // If the user is an admin, allow everything
      // if (verified) {
      //    return new RuleBuilder().allowAll().build();
      // }

      // By default, deny everything. This should never get hit, but it's
      // good to be defensive
      return new RuleBuilder().denyAll().build();
   }
}
