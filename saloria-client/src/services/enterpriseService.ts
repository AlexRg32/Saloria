import { apiClient } from '../lib/axios';

export interface EnterpriseReadiness {
    publicProfileReady: boolean;
    bookingReady: boolean;
    publicProfilePath?: string | null;
    missingPublicProfile: string[];
    missingBookingSetup: string[];
}

export interface Enterprise {
    id: number;
    name: string;
    slug?: string;
    cif: string;
    address: string;
    phone: string;
    email: string;
    website?: string;
    logo?: string;
    banner?: string;
    instagram?: string;
    facebook?: string;
    tiktok?: string;
    whatsapp?: string;
    primaryColor?: string;
    secondaryColor?: string;
    description?: string;
    readiness?: EnterpriseReadiness;
}

export const enterpriseService = {
    getById: async (id: number) => {
        const response = await apiClient.get<Enterprise>(`/api/enterprises/${id}`);
        return response.data;
    },

    update: async (id: number, enterprise: Enterprise) => {
        const response = await apiClient.put<Enterprise>(`/api/enterprises/${id}`, enterprise);
        return response.data;
    },

    getEmployees: async (id: number) => {
        const response = await apiClient.get<any[]>(`/api/enterprises/${id}/employees`);
        return response.data;
    },

    getBySlug: async (slug: string) => {
        const response = await apiClient.get<Enterprise>(`/api/public/enterprises/slug/${slug}`);
        return response.data;
    }
};
