package com.vaadin.guice.security;

public interface PathAccessEvaluator {
    /**
    * Evaluates if the current user is granted the given permission. The format
    * of 'permission' is not specified and up to the implementation.
     *
     * <p>
     *     class RoleBasedAccessEvaluator implements PathAccessEvaluator {
     *         public boolean isGranted(String permission){
     *             if("admin".equals(permission)){
     *                 return User.getCurrentUser().isAdmin();
     *             }
     *             ...
     *         }
     *     }
     * </p>
    * */
    boolean isGranted(String permission);
}
