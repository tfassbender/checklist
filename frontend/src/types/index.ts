export interface User {
  username: string;
}

export interface CheckListItem {
  id: string;
  description: string;
  checked: boolean;
  orderIndex: number;
}

export interface CheckList {
  id: string;
  name: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
  items: CheckListItem[];
}

export interface CheckListSummary {
  id: string;
  name: string;
  itemCount: number;
  checkedCount: number;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
}

export interface CreateListRequest {
  name: string;
}

export interface UpdateListRequest {
  name?: string;
  notes?: string;
}

export interface CreateItemRequest {
  description: string;
}

export interface UpdateItemRequest {
  description?: string;
  checked?: boolean;
}

export interface ReorderItemsRequest {
  itemIds: string[];
}

export interface ApiError {
  message: string;
  status: number;
}
