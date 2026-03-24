import React, { useState } from 'react';
import { ServiceOffering } from '../../services/serviceOfferingService';
import { X, Tag } from 'lucide-react';
import { SearchableSelect, Option } from '../ui/SearchableSelect';

interface CreateServiceModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (service: ServiceOffering, imageFile?: File) => Promise<void>;
    enterpriseId: number;
}

const categoryOptions: Option[] = [
    { value: 'Corte', label: 'Corte' },
    { value: 'Barba', label: 'Barba' },
    { value: 'Color', label: 'Color' },
    { value: 'Tratamiento', label: 'Tratamiento' },
    { value: 'Otro', label: 'Otro' }
];

export const CreateServiceModal: React.FC<CreateServiceModalProps> = ({ isOpen, onClose, onSubmit, enterpriseId }) => {
    const [formData, setFormData] = useState<Partial<ServiceOffering>>({
        name: '',
        description: '',
        price: 0,
        duration: 30,
        category: 'Corte',
        image: ''
    });
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    if (!isOpen) return null;

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            
            // 5MB limit
            if (file.size > 5 * 1024 * 1024) {
                alert("El archivo es demasiado grande. El máximo permitido son 5MB.");
                e.target.value = ""; // Reset input
                return;
            }

            setSelectedFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreviewUrl(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            await onSubmit({ ...formData, enterpriseId } as ServiceOffering, selectedFile || undefined);
            setFormData({
                name: '',
                description: '',
                price: 0,
                duration: 30,
                category: 'Corte',
                image: ''
            });
            setSelectedFile(null);
            setPreviewUrl(null);
            onClose();
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-start sm:items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm overflow-y-auto custom-scrollbar">
            <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl overflow-visible animate-in fade-in zoom-in duration-200 my-auto">
                <div className="bg-slate-900 px-8 py-6 text-white relative rounded-t-[16px]">
                    <h2 className="text-2xl font-bold tracking-tight">Nuevo Servicio</h2>
                    <p className="text-slate-400 text-sm mt-1">Define los detalles del nuevo servicio para tu negocio.</p>
                    <button onClick={onClose} className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors">
                        <X size={24} />
                    </button>
                </div>
                
                <form onSubmit={handleSubmit} className="p-8 space-y-6">
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-slate-700 ml-1">Nombre del Servicio</label>
                        <input 
                            type="text" 
                            required
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all placeholder:text-slate-400"
                            value={formData.name}
                            onChange={(e) => setFormData({...formData, name: e.target.value})}
                            placeholder="Ej: Corte Clásico"
                        />
                    </div>
                    
                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-2">
                             <label className="text-sm font-semibold text-slate-700 ml-1">Precio (€)</label>
                             <input 
                                type="number" 
                                required
                                min="0"
                                className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                value={formData.price}
                                onChange={(e) => setFormData({...formData, price: Number(e.target.value)})}
                             />
                        </div>
                        <div className="space-y-2">
                             <label className="text-sm font-semibold text-slate-700 ml-1">Duración (min)</label>
                             <input 
                                type="number" 
                                required
                                min="5"
                                step="5"
                                className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                value={formData.duration}
                                onChange={(e) => setFormData({...formData, duration: Number(e.target.value)})}
                             />
                        </div>
                    </div>

                    <SearchableSelect
                        label="Categoría"
                        options={categoryOptions}
                        value={formData.category || ''}
                        onChange={(val) => setFormData({ ...formData, category: val as any })}
                        icon={Tag}
                        required
                    />

                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-slate-700 ml-1">Imagen del Servicio</label>
                        <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-2xl border border-dashed border-slate-200">
                            {previewUrl ? (
                                <img src={previewUrl} alt="Preview" className="w-20 h-20 rounded-xl object-cover border border-white shadow-sm" />
                            ) : (
                                <div className="w-20 h-20 rounded-xl bg-slate-100 flex items-center justify-center text-slate-400">
                                    <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                                </div>
                            )}
                            <div className="flex-1">
                                <input 
                                    type="file" 
                                    accept="image/*"
                                    className="block w-full text-xs text-slate-500
                                      file:mr-4 file:py-2 file:px-4
                                      file:rounded-full file:border-0
                                      file:text-xs file:font-bold
                                      file:bg-brand-primary/10 file:text-brand-primary
                                      hover:file:bg-brand-primary/20 transition-all cursor-pointer
                                    "
                                    onChange={handleFileChange}
                                />
                                <p className="mt-1 text-[10px] text-slate-400 uppercase font-bold tracking-wider">PNG, JPG hasta 5MB</p>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-slate-700 ml-1">Descripción</label>
                        <textarea 
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all h-28 resize-none placeholder:text-slate-400"
                            value={formData.description}
                            onChange={(e) => setFormData({...formData, description: e.target.value})}
                            placeholder="Describe brevemente el servicio..."
                        />
                    </div>

                    <div className="pt-6 flex gap-4 border-t border-slate-100">
                        <button 
                            type="button" 
                            onClick={onClose}
                            className="flex-1 px-6 py-3 rounded-xl text-slate-600 font-semibold hover:bg-slate-50 transition-colors"
                        >
                            Cancelar
                        </button>
                        <button 
                            type="submit" 
                            disabled={loading}
                            className="flex-2 px-8 py-3 rounded-xl bg-brand-primary text-slate-900 font-bold transition-all disabled:opacity-50 shadow-brand active:scale-95"
                        >
                            {loading ? 'Guardando...' : 'Crear Servicio'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
