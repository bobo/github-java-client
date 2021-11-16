package com.spotify.github.v3.clients;

import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.git.Tree;
import com.spotify.github.v3.hooks.requests.WebhookCreate;
import com.spotify.github.v3.repos.*;
import com.spotify.github.v3.repos.requests.AuthenticatedUserRepositoriesFilter;
import com.spotify.github.v3.repos.requests.RepositoryCreateStatus;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RepositoryClient {
    CompletableFuture<Repository> getRepository();

    CompletableFuture<List<Repository>> listOrganizationRepositories();

    Iterator<AsyncPage<Repository>> listAuthenticatedUserRepositories(
            AuthenticatedUserRepositoriesFilter filter);

    CompletableFuture<Boolean> isCollaborator(String user);

    CompletableFuture<Void> createWebhook(
            WebhookCreate request, boolean ignoreExisting);

    CompletableFuture<Void> setCommitStatus(
            String sha, RepositoryCreateStatus request);

    CompletableFuture<CommitStatus> getCommitStatus(String ref);

    CompletableFuture<List<Status>> listCommitStatuses(String sha);

    Iterator<AsyncPage<Status>> listCommitStatuses(String sha, int itemsPerPage);

    CompletableFuture<List<CommitItem>> listCommits();

    CompletableFuture<Commit> getCommit(String sha);

    @Deprecated
    CompletableFuture<Tree> getTree(String sha);

    CompletableFuture<Content> getFileContent(String path);

    CompletableFuture<Content> getFileContent(String path, String ref);

    CompletableFuture<List<FolderContent>> getFolderContent(String path);

    CompletableFuture<Comment> createComment(String sha, String body);

    CompletableFuture<Comment> getComment(int id);

    CompletableFuture<List<FolderContent>> getFolderContent(
            String path, String ref);

    CompletableFuture<CommitComparison> compareCommits(String base, String head);

    CompletableFuture<Branch> getBranch(String branch);

    CompletableFuture<List<Branch>> listBranches();

    CompletableFuture<Void> deleteComment(int id);

    CompletableFuture<Void> editComment(int id, String body);

    CompletableFuture<Languages> getLanguages();

    CompletableFuture<Optional<CommitItem>> merge(String base, String head);

    CompletableFuture<Optional<CommitItem>> merge(
            String base, String head, String commitMessage);

    CompletableFuture<Repository> createFork(String organization);

    String getContentPath(String path, String query);
}
