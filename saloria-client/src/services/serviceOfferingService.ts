import { apiClient } from '../lib/axios';

export interface ServiceOffering {
    id?: number;
    name: string;
    description: string;
    price: number;
    image?: string | null;
    duration: number; // in minutes
    category: string;
    enterpriseId: number;
}

export const serviceOfferingService = {
    getAllByEnterprise: async (enterpriseId: number) => {
        const response = await apiClient.get<ServiceOffering[]>(`/api/services/${enterpriseId}`);
        return response.data;
    },

    create: async (service: ServiceOffering, imageFile?: File) => {
        const formData = new FormData();

        formData.append(
            'service',
            new Blob([JSON.stringify(service)], { type: 'application/json' })
        );

        if (imageFile) {
            formData.append('image', imageFile);
        }

        const response = await apiClient.post<ServiceOffering>('/api/services', formData);
        return response.data;
    },

    delete: async (enterpriseId: number, id: number) => {
        await apiClient.delete(`/api/services/${enterpriseId}/${id}`);
    }
};
