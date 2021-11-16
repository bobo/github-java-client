package com.spotify.github;

import com.spotify.folsom.MemcacheClient;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.clients.RepositoryClient;
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

public class CachedRepositoryClient implements RepositoryClient {

    private final static int TTL =60*5;

    final MemcacheClient<String> memcachedClient;
    private final RepositoryClient baseClient;
    private final Json json = Json.create();

    public CachedRepositoryClient(MemcacheClient<String> memcacheClient, RepositoryClient baseClient) {
        this.memcachedClient = memcacheClient;
        this.baseClient = baseClient;
    }

    @Override
    public CompletableFuture<Repository> getRepository() {
        return baseClient.getRepository();
    }

    @Override
    public CompletableFuture<List<Repository>> listOrganizationRepositories() {
        return baseClient.listOrganizationRepositories();
    }

    @Override
    public Iterator<AsyncPage<Repository>> listAuthenticatedUserRepositories(AuthenticatedUserRepositoriesFilter filter) {
        return baseClient.listAuthenticatedUserRepositories(filter);
    }

    @Override
    public CompletableFuture<Boolean> isCollaborator(String user) {
        String memcachedKey = "isCollaborator:" + user;
        return memcachedClient.get(memcachedKey).toCompletableFuture().thenCompose(value -> {
            if (value != null) {
                return createFromMemcached(value, Boolean.class);
            }
            CompletableFuture<Boolean> githubResult = baseClient.isCollaborator(user);
            storeResultAsync(memcachedKey,githubResult);
            return githubResult;
        });
    }

    @Override
    public CompletableFuture<Commit> getCommit(String sha) {
        String memcachedKey = "getCommit:" + sha;
        return memcachedClient.get(memcachedKey).toCompletableFuture().thenCompose(value -> {
            if (value != null) {
                return createFromMemcached(value, Commit.class);
            }
            CompletableFuture<Commit> githubResult = baseClient.getCommit(sha);
            storeResultAsync(memcachedKey, githubResult);
            return githubResult;
        });
    }


    @Override
    public CompletableFuture<Void> createWebhook(WebhookCreate request, boolean ignoreExisting) {
        return baseClient.createWebhook(request, ignoreExisting);
    }

    @Override
    public CompletableFuture<Void> setCommitStatus(String sha, RepositoryCreateStatus request) {
        return baseClient.setCommitStatus(sha, request);
    }

    @Override
    public CompletableFuture<CommitStatus> getCommitStatus(String ref) {
        String memcachedKey = "getCommitStatus:" + ref;
        return memcachedClient.get(memcachedKey).toCompletableFuture().thenCompose(value -> {
            if (value != null) {
                return createFromMemcached(value, CommitStatus.class);
            }
            CompletableFuture<CommitStatus> githubResult = baseClient.getCommitStatus(ref);
            storeResultAsync(memcachedKey, githubResult);
            return githubResult;
        });
    }

    @Override
    public CompletableFuture<List<Status>> listCommitStatuses(String sha) {
        return baseClient.listCommitStatuses(sha);
    }

    @Override
    public Iterator<AsyncPage<Status>> listCommitStatuses(String sha, int itemsPerPage) {
        return baseClient.listCommitStatuses(sha, itemsPerPage);
    }

    @Override
    public CompletableFuture<List<CommitItem>> listCommits() {
        return baseClient.listCommits();
    }


    @Override
    public CompletableFuture<Tree> getTree(String sha) {
        return baseClient.getTree(sha);
    }

    @Override
    public CompletableFuture<Content> getFileContent(String path) {
        return baseClient.getFileContent(path);
    }

    @Override
    public CompletableFuture<Content> getFileContent(String path, String ref) {
        return baseClient.getFileContent(path, ref);
    }

    @Override
    public CompletableFuture<List<FolderContent>> getFolderContent(String path) {
        return baseClient.getFolderContent(path);
    }

    @Override
    public CompletableFuture<Comment> createComment(String sha, String body) {
        return baseClient.createComment(sha, body);
    }

    @Override
    public CompletableFuture<Comment> getComment(int id) {
        return baseClient.getComment(id);
    }

    @Override
    public CompletableFuture<List<FolderContent>> getFolderContent(String path, String ref) {
        return baseClient.getFolderContent(path, ref);
    }

    @Override
    public CompletableFuture<CommitComparison> compareCommits(String base, String head) {
        return baseClient.compareCommits(base, head);
    }

    @Override
    public CompletableFuture<Branch> getBranch(String branch) {
        String memcachedKey = "getBranch:" + branch;
        return memcachedClient.get(memcachedKey).toCompletableFuture().thenCompose(value -> {
            if (value != null) {
                return createFromMemcached(value, Branch.class);
            }
            CompletableFuture<Branch> githubResult = baseClient.getBranch(branch);
            storeResultAsync(memcachedKey, githubResult);
            return githubResult;
        });
    }

    @Override
    public CompletableFuture<List<Branch>> listBranches() {
        return baseClient.listBranches();
    }

    @Override
    public CompletableFuture<Void> deleteComment(int id) {
        return baseClient.deleteComment(id);
    }

    @Override
    public CompletableFuture<Void> editComment(int id, String body) {
        return baseClient.editComment(id, body);
    }

    @Override
    public CompletableFuture<Languages> getLanguages() {
        return baseClient.getLanguages();
    }

    @Override
    public CompletableFuture<Optional<CommitItem>> merge(String base, String head) {
        return baseClient.merge(base, head);
    }

    @Override
    public CompletableFuture<Optional<CommitItem>> merge(String base, String head, String commitMessage) {
        return baseClient.merge(base, head, commitMessage);
    }

    @Override
    public CompletableFuture<Repository> createFork(String organization) {
        return baseClient.createFork(organization);
    }

    @Override
    public String getContentPath(String path, String query) {
        return baseClient.getContentPath(path, query);
    }



    private <T> CompletableFuture<T> createFromMemcached(String value, Class<T> type) {
        return CompletableFuture.completedFuture(json.fromJsonUnchecked(value, type));
    }

    private void storeResultAsync(String memcachedKey, CompletableFuture result) {
        result.thenAcceptAsync(commit -> memcachedClient.set(memcachedKey, json.toJsonUnchecked(commit), TTL));
    }
}
