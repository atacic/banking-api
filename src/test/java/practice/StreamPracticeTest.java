package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;

public class StreamPracticeTest {

    public record User(Long id, String name, int age, boolean active) {}

    private final List<User> users = List.of(
            new User(1L, "Milan", 28, true),
            new User(2L, "Ana", 17, true),
            new User(3L, "Nikola", 35, false),
            new User(4L, "Jovan", 22, true)
    );

    @Test
    void shouldReturnNamesOfActiveAdults() {
        List<String> result = users.stream()
                .filter(User::active)
                .filter(u -> u.age() >= 18)
                .map(User::name)
                .toList();

        assertEquals(List.of("Milan", "Jovan"), result);
    }

    @Test
    void shouldFindFirstInactiveUser() {
        String result = users.stream()
                .filter(u -> !u.active())
                .map(User::name)
                .findFirst()
                .orElse("NONE");

        assertEquals("Nikola", result);
    }

    @Test
    void shouldGroupUsersByActiveStatus() {
        Map<Boolean, List<User>> grouped =
                users.stream().collect(groupingBy(User::active));

        assertEquals(3, grouped.get(true).size());
        assertEquals(1, grouped.get(false).size());
    }

    @Test
    void shouldCalculateAverageAndSumOfAges() {
        int sum = users.stream().mapToInt(User::age).sum();

        double avg = users.stream()
                .collect(averagingInt(User::age));

        assertEquals(28 + 17 + 35 + 22, sum);
        assertEquals(25.5, avg);
    }

    @Test
    void shouldSortActiveUsersByAgeDescending() {
        List<String> result =
                users.stream()
                        .filter(User::active)
                        .sorted((a, b) -> Integer.compare(b.age(), a.age()))
                        .map(User::name)
                        .toList();

        assertEquals(List.of("Milan", "Jovan", "Ana"), result);
    }

    @Test
    void shouldFlattenAndDistinctSkills() {
        List<List<String>> skills = List.of(
                List.of("Java", "Docker"),
                List.of("Java", "Spring"),
                List.of("Kubernetes", "Docker")
        );

        List<String> unique = skills.stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .toList();

        assertEquals(List.of("Docker", "Java", "Kubernetes", "Spring"), unique);
    }

    @Test
    void shouldPartitionAdultsAndMinors() {
        var partition =
                users.stream()
                        .collect(partitioningBy(u -> u.age() >= 18));

        assertEquals(3, partition.get(true).size());
        assertEquals(1, partition.get(false).size());
    }

    @Test
    void shouldReturnTop2OldestActiveUsers() {
        List<String> result = users.stream()
                .filter(User::active)
                .sorted((a, b) -> Integer.compare(b.age(), a.age()))
                .map(User::name)
                .limit(2)
                .toList();

        assertEquals(List.of("Milan", "Jovan"), result);
    }

    @Test
    void shouldCheckIfAllActiveUsersAreAdults() {
        boolean allAdults =
                users.stream()
                        .filter(User::active)
                        .allMatch(u -> u.age() >= 18);

        assertFalse(allAdults);
    }

    @Test
    void shouldBuildMapIdToName() {
        Map<Long, String> map =
                users.stream()
                        .collect(toMap(User::id, User::name));

        assertEquals("Milan", map.get(1L));
        assertEquals(4, map.size());
    }

    @Test
    void shouldCountNamesStartingWithM() {
        long count = users.stream()
                .map(User::name)
                .filter(n -> n.startsWith("M"))
                .count();

        assertEquals(1, count);
    }

    @Test
    void shouldReturnCommaSeparatedNames() {
        String result = users.stream()
                .map(User::name)
                .collect(joining(", "));

        assertEquals("Milan, Ana, Nikola, Jovan", result);
    }
}