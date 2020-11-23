/* (C)2020 */
package ru.mail.jira.plugins.jsincluder;

public enum Context {
  CREATE,
  VIEW,
  EDIT,
  TRANSITION;

  static Context parseContext(String contextValue) {
    try {
      return valueOf(contextValue.toUpperCase());
    } catch (Exception e) {
      return null;
    }
  }
}
