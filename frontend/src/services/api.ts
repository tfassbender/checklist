import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  CheckList,
  CheckListSummary,
  CreateListRequest,
  UpdateListRequest,
  CreateItemRequest,
  UpdateItemRequest,
  ReorderItemsRequest,
  CheckListItem,
} from '../types'

const API_BASE = '/api'

let authToken: string | null = null

export function setAuthToken(token: string | null) {
  authToken = token
}

async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`
  }

  // Merge additional headers from options
  if (options.headers) {
    const optHeaders = options.headers as Record<string, string>
    Object.assign(headers, optHeaders)
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}))
    const error = new Error(errorData.message || `HTTP error ${response.status}`)
    ;(error as Error & { status: number }).status = response.status
    throw error
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json()
}

// Auth endpoints
export async function login(request: LoginRequest): Promise<AuthResponse> {
  return fetchApi<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function register(request: RegisterRequest): Promise<AuthResponse> {
  return fetchApi<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

// List endpoints
export async function getLists(): Promise<CheckListSummary[]> {
  return fetchApi<CheckListSummary[]>('/lists')
}

export async function getList(id: string): Promise<CheckList> {
  return fetchApi<CheckList>(`/lists/${id}`)
}

export async function createList(request: CreateListRequest): Promise<CheckList> {
  return fetchApi<CheckList>('/lists', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function updateList(
  id: string,
  request: UpdateListRequest
): Promise<CheckList> {
  return fetchApi<CheckList>(`/lists/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export async function deleteList(id: string): Promise<void> {
  return fetchApi<void>(`/lists/${id}`, {
    method: 'DELETE',
  })
}

export async function resetList(id: string): Promise<CheckList> {
  return fetchApi<CheckList>(`/lists/${id}/reset`, {
    method: 'POST',
  })
}

export async function activateList(id: string): Promise<CheckList> {
  return fetchApi<CheckList>(`/lists/${id}/activate`, {
    method: 'PUT',
  })
}

export async function deactivateList(id: string): Promise<CheckList> {
  return fetchApi<CheckList>(`/lists/${id}/deactivate`, {
    method: 'PUT',
  })
}

// Item endpoints
export async function addItem(
  listId: string,
  request: CreateItemRequest
): Promise<CheckListItem> {
  return fetchApi<CheckListItem>(`/lists/${listId}/items`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function updateItem(
  listId: string,
  itemId: string,
  request: UpdateItemRequest
): Promise<CheckListItem> {
  return fetchApi<CheckListItem>(`/lists/${listId}/items/${itemId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export async function deleteItem(listId: string, itemId: string): Promise<void> {
  return fetchApi<void>(`/lists/${listId}/items/${itemId}`, {
    method: 'DELETE',
  })
}

export async function reorderItems(
  listId: string,
  request: ReorderItemsRequest
): Promise<CheckList> {
  return fetchApi<CheckList>(`/lists/${listId}/items/reorder`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}
