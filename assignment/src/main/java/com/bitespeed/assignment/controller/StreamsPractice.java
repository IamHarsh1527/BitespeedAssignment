package com.bitespeed.assignment.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StreamsPractice {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        //even numbers using streams
        List<Integer> even = numbers.stream()
                .filter(i->i%2==0)
                .toList();
        System.out.println(even);
        //odd numbers using streams
        List<Integer> odd = numbers.stream()
                .filter(i->i%2!=0)
                .toList();
        System.out.println(odd);

        //sum of all numbers using streams
        int sum = numbers.stream()
                .reduce(0, Integer::sum);
        System.out.println(sum);

        //sum of even numbers using streams
        int evenSum = numbers.stream()
                .filter(i->i%2==0)
                .reduce(0, Integer::sum);
        System.out.println(evenSum);

        List<String> names = List.of("Alice", "Bob", "jason", "jude", "john", "jane");
        //names starting with 'j' using streams
        List<String> jnames=names.stream()
                .filter(name -> name.toLowerCase().startsWith("j"))
                .toList();
        System.out.println(jnames);
        int countJ = names.stream()
                .filter(n->n.toLowerCase().startsWith("j"))
                .mapToInt(n -> 1)
                .sum();
        System.out.println("Count of names starting with 'j': " + countJ);

        // len of all names using streams
        List<Integer> nameLengths = names.stream()
                .map(a->a.length())
                .toList();
        System.out.println(nameLengths);

        //numers squared using streams
        List<Integer> sqnum = numbers.stream()
                .map(i->i*i)
                .toList();
        System.out.println(sqnum);

        List<Integer> sqEven = numbers.stream()
                .map(i-> i*i)
                .filter(i->i>20)
                .toList();
        System.out.println(sqEven);

        //names in uppercase using streams
        List<String> upperName = names.stream()
                .map(name->name.toUpperCase())
                .toList();
        System.out.println(upperName);

        List<Double> prices = List.of(10.99, 20.49, 5.99, 15.00, 30.00);
        List<Integer> roundprices = prices.stream()
                .map(p-> (int) Math.round(p))
                .toList();
        System.out.println(roundprices);

        //find first even number greater than 5
        int num = numbers.stream()
                .filter(i->i>5 && i%2==0)
                .findFirst()
                .orElse(-1); // returns -1 if no such number exists
        System.out.println("First even number greater than 5: " + num);

        //find first name starting with 'j'
        String ifirstName = names.stream()
                .filter(name -> name.toLowerCase().startsWith("j"))
                .findFirst()
                .orElse("No name found starting with 'j'"); // returns a default message if no such name exists
        System.out.println("First name starting with 'z': " + ifirstName);

        //total number of characters in all names
        int totalChars = names.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("Total number of characters in all names: " + totalChars);

        //calculate product of all numbers
        int prod = numbers.stream()
                .reduce(1, (a, b) -> a * b);
        System.out.println("Product of all numbers: " + prod);

        //collect name larger than 4 characters into set
        Set<String> longNamesSet = names.stream()
                .filter(n->n.length()>4)
                .collect(Collectors.toSet());
        System.out.println("Names longer than 4 characters: " + longNamesSet);
                //longest name
                String longestName = names.stream()
                .reduce("", (a, b) -> a.length() > b.length() ? a : b);
        System.out.println("Longest name: " + longestName);

        //partition list of numbers into even and odd
        var partitioned = numbers.stream()
                .collect(Collectors.partitioningBy(i -> i % 2 == 0));
        System.out.println("Partitioned numbers: " + partitioned);
        //create a map of names and their lengths
        var nameLengthMap = names.stream()
                .collect(Collectors.toMap(name -> name, String::length));
        System.out.println("Name length map: " + nameLengthMap);
    }
}
