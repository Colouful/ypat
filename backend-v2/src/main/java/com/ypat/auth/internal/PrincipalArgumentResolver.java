package com.ypat.auth.internal;

import com.ypat.auth.api.Principal;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * PR-14 follow-up: argument resolver that lets controllers take
 * {@link Principal} directly in their method signatures:
 *
 *   @GetMapping("/me")
 *   public CurrentUser me(Principal principal) { ... }
 *
 * Returns the SecurityContext's Principal if present, else null.
 * Public endpoints can keep working — the controller treats null
 * as "unauthenticated" and returns 401 itself.
 */
@Component
public class PrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter p) {
        return Principal.class.equals(p.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter p,
                                  ModelAndViewContainer mav,
                                  NativeWebRequest req,
                                  WebDataBinderFactory binder) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof TokenAuthentication ta) {
            return ta.ypatPrincipal();
        }
        return null;
    }
}