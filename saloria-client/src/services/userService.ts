import { apiClient } from '../lib/axios';

export interface User {
    id: number;
    name: string;
    email: string;
    role: 'ADMIN' | 'EMPLEADO' | 'CLIENTE' | 'SUPER_ADMIN';
    enterpriseId?: number;
    enterprise?: { id: number; name?: string };
    phone?: string;
    active: boolean;
    password?: string;
}

export interface CreateUserPayload {
    name: string;
    email: string;
    password: string;
    role: User['role'];
    enterpriseId: number;
    phone?: string;
}

export interface UpdateUserPayload {
    name: string;
    email: string;
    role: User['role'];
    phone?: string;
    active?: boolean;
    password?: string;
}

export const userService = {
    getUsersByEnterprise: async (enterpriseId: number) => {
        const response = await apiClient.get<User[]>(`/api/users/${enterpriseId}`);
        return response.data;
    },
    createUser: async (user: CreateUserPayload) => {
        const response = await apiClient.post<User>('/api/users', user);
        return response.data;
    },
    updateUser: async (id: number, user: UpdateUserPayload) => {
        const response = await apiClient.put<User>(`/api/users/${id}`, user);
        return response.data;
    },
    deleteUser: async (id: number) => {
        await apiClient.delete(`/api/users/${id}`);
    }
};
