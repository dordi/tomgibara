package com.tomgibara.crinch.math;

public class CrinchMath {

	public static int gcd(int a, int b) {
		while (a != b) {
			if (a > b) {
				int na = a % b;
				if (na == 0) return b;
				a = na;
			} else {
				int nb = b % a;
				if (nb == 0) return a;
				b = nb;
			}
		}
		return a;
	}
	
	public static int lcd(int a, int b) {
		return a * b / gcd(a, b);
	}
	
	public static boolean isCoprime(int a, int b) {
		return gcd(a, b) == 1;
	}
	
	private CrinchMath() {}
	
}
