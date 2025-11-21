/**
 * API Service Unit Tests
 *
 * Tests the API type definitions and request/response structures.
 */

import { describe, it, expect } from 'vitest';
import type {
  RenameRequest,
  OrganizeRequest,
  ExtractRequest,
  PhotoOrganizeRequest,
  DuplicateRequest,
  FileSystemEntry,
} from '../types';

describe('API Types - Request Validation', () => {
  it('should validate RenameRequest structure', () => {
    const request: RenameRequest = {
      sourceDirectory: '/test',
      includeSubDirectories: false,
    };

    expect(request.sourceDirectory).toBe('/test');
    expect(request.includeSubDirectories).toBe(false);
  });

  it('should validate OrganizeRequest structure', () => {
    const request: OrganizeRequest = {
      sourceDirectory: '/source',
      destinationDirectory: '/dest',
    };

    expect(request.sourceDirectory).toBe('/source');
    expect(request.destinationDirectory).toBe('/dest');
  });

  it('should validate ExtractRequest structure', () => {
    const request: ExtractRequest = {
      sourceDirectory: '/source',
      destinationDirectory: '/dest',
    };

    expect(request.sourceDirectory).toBe('/source');
    expect(request.destinationDirectory).toBe('/dest');
  });

  it('should validate PhotoOrganizeRequest structure', () => {
    const request: PhotoOrganizeRequest = {
      sourceDirectory: '/photos',
      destinationDirectory: '/organized',
    };

    expect(request.sourceDirectory).toBe('/photos');
    expect(request.destinationDirectory).toBe('/organized');
  });

  it('should validate DuplicateRequest structure', () => {
    const request: DuplicateRequest = {
      directory: '/files',
    };

    expect(request.directory).toBe('/files');
  });
});

describe('API Types - Response Validation', () => {
  it('should validate FileSystemEntry structure', () => {
    const entry: FileSystemEntry = {
      path: '/home/user',
      name: 'user',
      directory: true,
      parent: false,
      readable: true,
      writable: true,
      size: undefined,
      lastModified: undefined,
    };

    expect(entry.path).toBe('/home/user');
    expect(entry.name).toBe('user');
    expect(entry.directory).toBe(true);
    expect(entry.parent).toBe(false);
    expect(entry.readable).toBe(true);
    expect(entry.writable).toBe(true);
  });

  it('should validate FileSystemEntry with size and lastModified', () => {
    const entry: FileSystemEntry = {
      path: '/home/user/file.txt',
      name: 'file.txt',
      directory: false,
      parent: false,
      readable: true,
      writable: true,
      size: 1024,
      lastModified: Date.now(),
    };

    expect(entry.path).toBe('/home/user/file.txt');
    expect(entry.name).toBe('file.txt');
    expect(entry.directory).toBe(false);
    expect(entry.size).toBe(1024);
    expect(entry.lastModified).toBeGreaterThan(0);
  });
});
