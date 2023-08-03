package com.linkedin.java_exception_handling;

public class FibonacciOutOfRangeException extends Exception{
  public FibonacciOutOfRangeException(String message) {
    super(message);
  }
}
