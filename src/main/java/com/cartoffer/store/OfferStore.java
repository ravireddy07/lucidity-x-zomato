package com.cartoffer.store;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OfferStore {
  // Immutable offer record
  public static record Offer(int restaurantId, String type, int value, Set<String> segments) {
  }

  // restaurant_id -> list of offers
  private final Map<Integer, List<Offer>> byRestaurant = new ConcurrentHashMap<>();

  /** Remove all offers (used by tests for a clean slate). */
  public synchronized void clear() {
    byRestaurant.clear();
  }

  /** Add/seed an offer for a restaurant. */
  public synchronized void addOffer(int restaurantId, String type, int value, Set<String> segments) {
    // normalize segments to lowercase to allow case-insensitive matching
    Set<String> norm = new HashSet<>();
    for (String s : segments) {
      if (s != null)
        norm.add(s.toLowerCase());
    }
    Offer offer = new Offer(restaurantId, type, value, Collections.unmodifiableSet(norm));
    byRestaurant.computeIfAbsent(restaurantId, k -> new ArrayList<>()).add(offer);
  }

  /** Get all offers matching (restaurantId, segment). */
  public List<Offer> getOffers(int restaurantId, String segment) {
    if (segment == null)
      return List.of();
    String seg = segment.toLowerCase();
    List<Offer> all = byRestaurant.getOrDefault(restaurantId, Collections.emptyList());
    if (all.isEmpty())
      return List.of();
    List<Offer> result = new ArrayList<>();
    for (Offer o : all) {
      if (o.segments().contains(seg)) {
        result.add(o);
      }
    }
    return result;
  }
}
