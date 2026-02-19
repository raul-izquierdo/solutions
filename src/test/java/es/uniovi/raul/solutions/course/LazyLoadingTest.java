package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uniovi.raul.solutions.course.naming.SolutionsDetectionStrategy;
import es.uniovi.raul.solutions.github.GithubApi;

class LazyLoadingTest {

    @Test
    @DisplayName("Group loads solutions lazily only when needed")
    void groupLoadsolutionsLazily() throws Exception {
        // Arrange
        GithubApi mockApi = mock(GithubApi.class);
        SolutionsDetectionStrategy mockIdentifier = mock(SolutionsDetectionStrategy.class);

        when(mockApi.fetchRepositoriesForTeam("org", "team-slug"))
                .thenReturn(List.of("org/solution1", "solution2", "other-repo"));
        when(mockIdentifier.isSolutionRepository("org/solution1")).thenReturn(true);
        when(mockIdentifier.isSolutionRepository("solution2")).thenReturn(true);
        when(mockIdentifier.isSolutionRepository("other-repo")).thenReturn(false);

        Group group = new Group("TestGroup", "team-slug", Optional.empty(),
                "org", mockApi, mockIdentifier);

        // Act & Assert
        // At this point, no API call should have been made yet
        verify(mockApi, never()).fetchRepositoriesForTeam(any(), any());

        // First access triggers the API call
        List<String> solutions = group.getAccesibleSolutions();

        // Verify API was called and results are correct
        verify(mockApi, times(1)).fetchRepositoriesForTeam("org", "team-slug");
        assertEquals(List.of("solution1", "solution2"), solutions);

        // Second access should use cached result
        assertTrue(group.hasAccessTo("solution1"));
        assertFalse(group.hasAccessTo("other-repo"));

        // Should still only have called the API once (cached)
        verify(mockApi, times(1)).fetchRepositoriesForTeam("org", "team-slug");
    }

    @Test
    @DisplayName("hasAccessTo works correctly with lazy loading")
    void hasAccessToWorksWithLazyLoading() throws Exception {
        // Arrange
        GithubApi mockApi = mock(GithubApi.class);
        SolutionsDetectionStrategy mockIdentifier = mock(SolutionsDetectionStrategy.class);

        when(mockApi.fetchRepositoriesForTeam("org", "team-slug"))
                .thenReturn(List.of("solution1"));
        when(mockIdentifier.isSolutionRepository("solution1")).thenReturn(true);

        Group group = new Group("TestGroup", "team-slug", Optional.empty(),
                "org", mockApi, mockIdentifier);

        // Act & Assert
        assertTrue(group.hasAccessTo("solution1"));
        assertFalse(group.hasAccessTo("nonexistent"));

        // Should have called API exactly once
        verify(mockApi, times(1)).fetchRepositoriesForTeam("org", "team-slug");
    }
}
