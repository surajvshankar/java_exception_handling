package com.linkedin.java_exception_handling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("fibonacci")
public class FibonacciController {
  // curl -i -X GET -H 'Content-Type: application/json' http://localhost:8080/fibonacci/findNumber?n=3
  /**
   *  Determine the n-th fibonacci number
   *
   * @param n position of the fibonacci number requested
   * @return the n-th fibonacci number in the sequence
  **/
  @GetMapping("findNumber")
  public ResponseEntity<Integer> findFibonacciNumber(@RequestParam int n) {
    return ResponseEntity.ok(fibonacci(n));
  }

  /**
   * Recursively find the Fibonacci number at the position in the sequence
   *
   * @param position requested position of the fibonacci sequence (example: 8)
   * @return fibonacci number at position (example: 21)
   */
  private int fibonacci(int position) {
    // --- Without this - it will cause a StackOverflowError - no exit criteria
    if (position <= 1) {
      return position;
    }
    // ---
    return fibonacci(position - 1) + fibonacci(position - 2);
  }

  // curl -i -X POST -H 'Content-Type: application/json' http://localhost:8080/fibonacci/createSequence?n=3
  /**
   * Store the first n-th numbers in the fibonacci sequence in a text file.
   *
   * @param n position in fibonacci sequence
   * @return name of the file created
   */
  @PostMapping("createSequence")
  public ResponseEntity<String> generateFibonacciSequence(@RequestParam int n) throws IOException {
    List<Integer> sequence = getSequence(n);
    return ResponseEntity.ok(storeSequence(sequence));
  }

  /**
   * Generate fibonacci sequence without using recursion
   *
   * @param n number of numbers that should be included in the fibonacci sequence
   * @return list of integers with fibonacci sequence
   */
  private List<Integer> getSequence(int n) {
    List<Integer> sequence = new ArrayList<>();
    sequence.add(0);
    int prev = 0;
    int curr = 1;
    int index = 0;
    while(++index <= n) {
      sequence.add(curr);
      curr = prev + curr;
      prev = curr - prev;
    }
    return sequence;
  }

  /**
   * Save the fibonacci sequence in a txt file
   *
   * @param sequence list of ints in the fibonacci sequence
   * @return String name of the file saved
   */
  private String storeSequence(List<Integer> sequence) throws IOException {
    String name = "fibonacci.txt";

    File file = new File(name);
    file.createNewFile();

    FileWriter writer = new FileWriter(file);
    writer.write(sequence.toString());
    writer.flush();
    writer.close();

    return name;
  }

  // curl -i -X GET -H 'Content-Type: application/json' http://localhost:8080/fibonacci/getSequence?filename=fibonacci.txt
  // curl -i -X GET -H 'Content-Type: application/json' http://localhost:8080/fibonacci/getSequence?filename=not_found.txt
  @GetMapping("getSequence")
  public ResponseEntity<String> retrieveFibonacciSequence(@RequestParam String filename) throws IOException {
    return ResponseEntity.ok(getSequence(filename));
  }
  // curl -i -X GET -H 'Content-Type: application/json' http://localhost:8080/fibonacci/getWithExceptionHandling?filename=not_found.txt
  @GetMapping("getWithExceptionHandling")
  public ResponseEntity<String> retrieveFibonacciSequenceWithExceptionHandling(@RequestParam String filename) {
    String sequence;
    try {
      sequence = getSequence(filename);
    } catch (FileNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not available. Please check the request and try again. " + e.getMessage());
    }
    return ResponseEntity.ok(sequence);
  }

  // Helper method
  private String getSequence(String filename) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    return reader.lines().collect(Collectors.joining());
  }

  // Challenge 1
  // curl -i -X POST -H 'Content-Type: application/json' http://localhost:8080/fibonacci/createSequenceWithExceptionHandling?n=3
  // curl -i -X POST -H 'Content-Type: application/json' http://localhost:8080/fibonacci/createSequenceWithExceptionHandling?n=b
  // curl -i -X POST -H 'Content-Type: application/json' http://localhost:8080/fibonacci/createSequenceWithExceptionHandling?n=npe
  @PostMapping("createSequenceWithExceptionHandling")
  public ResponseEntity<String> generateFibonacciSequenceWithExceptionHandling(@RequestParam String n) {
    String filename;
    try {
      List<Integer> sequence = getSequenceWithExceptionHandling(n); // Now returns a checked Exception
      filename = storeSequence(sequence);
    } catch (FibonacciInputException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (NullPointerException e) { // (RuntimeException e) {
      //  Ideally, you want to evaluate (using if-statements) input (is a int?) or return values (is null?),
      // and not catching it / raise an exception.
      return  ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("NPE");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
          body("Could not store the sequence in a file: " + e.getMessage());
//     } catch (Throwable e) {
      // Throwables should not be caught/handled - as it could include Errors, which are usually unrecoverable.
//     }
    } catch (Exception e) { // Catch-all Exception
      //  You will want to log details like metadata around parameters provided and/or the very specific
      // error message / stacktrace for easier debugging later.
      //  Also, a good place to setup metrics and alerting to notify the oncall.
      // May not want to return all this info to the user for security reasons, instead:
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("It is not you, it is us! Reach out to mail@domain.com");
    }
    return ResponseEntity.ok(filename);
  }
  private int fibonacciWithException(int position) throws FibonacciOutOfRangeException {
    if (position <= 1) {
      return position;
    }
    if (position >= 8) {
      throw new FibonacciOutOfRangeException(String.format("Requested position %s is too large. Please try again.", position));
    }
    return fibonacciWithException(position - 1) + fibonacciWithException(position - 2);
  }
  // curl -i -X GET -H 'Content-Type: application/json' http://localhost:8080/fibonacci/findNumberWithException?n=8
  @GetMapping("findNumberWithException")
  public ResponseEntity<String> findFibonacciNumberWithException(@RequestParam int n) {
    int result;
    try {
      result = fibonacciWithException(n);
    } catch (FibonacciOutOfRangeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    return ResponseEntity.ok(String.valueOf(result));
  }

  private List<Integer> getSequenceWithExceptionHandling(String str) throws FibonacciInputException {
    List<Integer> sequence;
    if (str.equals("npe")) {  // Simulate a NPE - runtime unchecked exception
//      sequence.add(0);
      String toCreateNPE = null;
      toCreateNPE.equals("test"); // This will throw a NPE
    }
    sequence = new ArrayList<>();
    int n;
    try {
      n = Integer.parseInt(str);
    } catch (NumberFormatException e) { // Unchecked Runtime
      throw new FibonacciInputException("Invalid input. Please provide a valid number");
    }
    sequence.add(0);
    int prev = 0;
    int curr = 1;
    int index = 0;
    while(++index <= n) {
      sequence.add(curr);
      curr = prev + curr;
      prev = curr - prev;
    }
    return sequence;
  }

  // Challenge 2
  // curl -i -X GET -H 'Content-Type: application/json' http://localhost:8080/fibonacci/findRatio?n=8
  /**
   * Determine the golden ratio for a given index in an array
   *
   * @param n position of the fibonacci number requested
   * @return the golden ratio (1.618033988749895) between n index and n-1 index
   */
  @GetMapping("findRatio")
  public ResponseEntity<String> getRatio(@RequestParam int n) {
    int dividend = fibonacci(n);
    int divisor = fibonacci(n-1);
    if (n == 1) { //  To catch an ArithmeticException - works only for result==int
                  // If result==double, no exception is thrown - instead infinity is returned.
                  // For double:
                  // IEEE 754 defines 1.0 / 0.0 as Infinity and -1.0 / 0.0 as -Infinity and 0.0 / 0.0 as NaN.
      int result;
      try {
        result = dividend / divisor;
      } catch (ArithmeticException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
            body(String.format("Division by Zero, produced by the fibonacci of %d", n-1));
      }
    }
    double result = (double) dividend / divisor;
    // Cast to double - To prevent "Integer division in floating-point context" in IDE.

    return ResponseEntity.ok(String.valueOf(result));
  }
}
