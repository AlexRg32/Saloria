
import { useState, useEffect } from 'react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { enterpriseService, Enterprise } from '@/services/enterpriseService';
import { 
    Store, 
    Palette, 
    Share2, 
    Save, 
    Loader2, 
    CheckCircle2, 
    AlertCircle,
    Instagram,
    Facebook,
    Phone,
    MapPin,
    Mail,
    Globe2,
    Info,
    Clock,
    ExternalLink,
    Rocket
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { WorkingHourSettings } from '@/components/settings/WorkingHourSettings';

const EnterprisePage = () => {
    const { user, updateBranding } = useAuth();
    const [enterprise, setEnterprise] = useState<Enterprise | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);
    const [activeTab, setActiveTab] = useState<'general' | 'branding' | 'social' | 'schedule'>('general');

    useEffect(() => {
        if (user?.enterpriseId) {
            loadEnterprise(user.enterpriseId);
        }
    }, [user?.enterpriseId]);

    const loadEnterprise = async (id: number) => {
        try {
            const data = await enterpriseService.getById(id);
            setEnterprise(data);
        } catch (error) {
            console.error('Error loading enterprise:', error);
            setMessage({ type: 'error', text: 'Error al cargar los datos de la empresa' });
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!enterprise || !user?.enterpriseId) return;

        setSaving(true);
        setMessage(null);
        try {
            const updated = await enterpriseService.update(user.enterpriseId, enterprise);
            setEnterprise(updated);
            
            // Immediate branding update
            if (updated.primaryColor || updated.secondaryColor) {
                updateBranding(updated.primaryColor, updated.secondaryColor);
            }

            setMessage({ type: 'success', text: 'Cambios guardados correctamente' });
            setTimeout(() => setMessage(null), 3000);
        } catch (error) {
            console.error('Error saving enterprise:', error);
            const backendMessage = (error as any)?.response?.data?.message;
            setMessage({ type: 'error', text: backendMessage || 'Error al guardar los cambios' });
        } finally {
            setSaving(false);
        }
    };

    const handleChange = (field: keyof Enterprise, value: string) => {
        if (!enterprise) return;
        setEnterprise({ ...enterprise, [field]: value });
    };

    if (loading) {
        return (
            <div className="flex h-[60vh] items-center justify-center">
                <Loader2 className="animate-spin text-brand-primary" size={48} />
            </div>
        );
    }

    if (!enterprise) {
        return (
            <div className="p-8 text-center text-slate-500">
                <AlertCircle className="mx-auto mb-4 opacity-20" size={64} />
                <p>No se encontró la información de tu peluquería.</p>
            </div>
        );
    }

    const tabs = [
        { id: 'general', label: 'Información General', icon: Info },
        { id: 'branding', label: 'Imagen y Marca', icon: Palette },
        { id: 'schedule', label: 'Horarios de Apertura', icon: Clock },
        { id: 'social', label: 'Redes Sociales', icon: Share2 },
    ];
    const readiness = enterprise.readiness;

    return (
        <section className="max-w-5xl mx-auto space-y-8 pb-12">
            <header className="flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-slate-900 tracking-tight">
                        Configuración del Negocio
                    </h1>
                    <p className="text-slate-500 mt-1">
                        Gestiona la identidad visual y la información pública de tu negocio.
                    </p>
                </div>
                
                <button
                    onClick={handleSave}
                    disabled={saving}
                    className="flex items-center gap-2 bg-brand-primary hover:bg-brand-primary/90 text-white px-6 py-2.5 rounded-xl font-semibold shadow-brand transition-all active:scale-95 disabled:opacity-50"
                >
                    {saving ? <Loader2 className="animate-spin" size={18} /> : <Save size={18} />}
                    {saving ? 'Guardando...' : 'Guardar Cambios'}
                </button>
            </header>

            <AnimatePresence>
                {message && (
                    <motion.div
                        initial={{ opacity: 0, y: -20 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -20 }}
                        className={`p-4 rounded-xl flex items-center gap-3 ${
                            message.type === 'success' 
                                ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' 
                                : 'bg-red-50 text-red-700 border border-red-100'
                        }`}
                    >
                        {message.type === 'success' ? <CheckCircle2 size={20} /> : <AlertCircle size={20} />}
                        <span className="font-medium">{message.text}</span>
                    </motion.div>
                )}
            </AnimatePresence>

            {readiness && (
                <div className="grid gap-4 md:grid-cols-2">
                    <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-lg shadow-slate-200/40">
                        <div className="flex items-start justify-between gap-4">
                            <div>
                                <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Perfil Público</p>
                                <h2 className="mt-2 text-xl font-black text-slate-900">Marketplace y ficha pública</h2>
                            </div>
                            <span className={`rounded-full px-3 py-1 text-xs font-black uppercase tracking-wider ${
                                readiness.publicProfileReady
                                    ? 'bg-emerald-50 text-emerald-700'
                                    : 'bg-amber-50 text-amber-700'
                            }`}>
                                {readiness.publicProfileReady ? 'Listo' : 'Pendiente'}
                            </span>
                        </div>

                        <p className="mt-3 text-sm text-slate-500">
                            {readiness.publicProfileReady
                                ? 'Tu negocio ya cumple el mínimo para aparecer en el directorio público.'
                                : 'Completa estos básicos para que tu negocio se publique con una ficha sólida.'}
                        </p>

                        {readiness.publicProfilePath && (
                            <a
                                href={readiness.publicProfilePath}
                                target="_blank"
                                rel="noreferrer"
                                className="mt-4 inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-2.5 text-sm font-bold text-white transition-colors hover:bg-slate-800"
                            >
                                <ExternalLink size={16} />
                                Ver perfil público
                            </a>
                        )}

                        <div className="mt-5 space-y-2">
                            {readiness.missingPublicProfile.length > 0 ? readiness.missingPublicProfile.map((item) => (
                                <div key={item} className="flex items-start gap-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
                                    <AlertCircle size={16} className="mt-0.5 shrink-0 text-amber-500" />
                                    <span>{item}</span>
                                </div>
                            )) : (
                                <div className="flex items-start gap-3 rounded-2xl bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                                    <CheckCircle2 size={16} className="mt-0.5 shrink-0" />
                                    <span>Tu ficha pública ya tiene lo necesario para mostrarse en el marketplace.</span>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-lg shadow-slate-200/40">
                        <div className="flex items-start justify-between gap-4">
                            <div>
                                <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Reserva Online</p>
                                <h2 className="mt-2 text-xl font-black text-slate-900">Preparación para aceptar citas</h2>
                            </div>
                            <span className={`rounded-full px-3 py-1 text-xs font-black uppercase tracking-wider ${
                                readiness.bookingReady
                                    ? 'bg-emerald-50 text-emerald-700'
                                    : 'bg-sky-50 text-sky-700'
                            }`}>
                                {readiness.bookingReady ? 'Activa' : 'En preparación'}
                            </span>
                        </div>

                        <p className="mt-3 text-sm text-slate-500">
                            {readiness.bookingReady
                                ? 'Tu negocio ya tiene servicios, profesionales y horarios suficientes para aceptar reservas.'
                                : 'Cuando cierres estos puntos, los clientes podrán reservar sin bloqueos desde tu perfil público.'}
                        </p>

                        <div className="mt-5 space-y-2">
                            {readiness.missingBookingSetup.length > 0 ? readiness.missingBookingSetup.map((item) => (
                                <div key={item} className="flex items-start gap-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
                                    <Rocket size={16} className="mt-0.5 shrink-0 text-sky-500" />
                                    <span>{item}</span>
                                </div>
                            )) : (
                                <div className="flex items-start gap-3 rounded-2xl bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                                    <CheckCircle2 size={16} className="mt-0.5 shrink-0" />
                                    <span>La reserva online está lista para funcionar en tu perfil.</span>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                {/* Sidebar Navigation */}
                <div className="lg:col-span-1 space-y-1">
                    {tabs.map((tab) => {
                        const Icon = tab.icon;
                        return (
                            <button
                                key={tab.id}
                                onClick={() => setActiveTab(tab.id as any)}
                                className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                                    activeTab === tab.id 
                                        ? 'bg-slate-900 text-white shadow-lg' 
                                        : 'text-slate-600 hover:bg-slate-100'
                                }`}
                            >
                                <Icon size={20} />
                                <span className="font-medium">{tab.label}</span>
                            </button>
                        );
                    })}
                </div>

                {/* Main Form Content */}
                <div className="lg:col-span-3 bg-white rounded-3xl p-8 shadow-xl shadow-slate-200/50 border border-slate-100">
                    <form onSubmit={handleSave} className="space-y-8">
                        {activeTab === 'general' && (
                            <motion.div 
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                className="space-y-6"
                            >
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2 col-span-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">Nombre Comercial</label>
                                        <div className="relative">
                                            <Store className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                            <input
                                                type="text"
                                                value={enterprise.name}
                                                onChange={(e) => handleChange('name', e.target.value)}
                                                className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                                placeholder="Ej. Barbería El Elegante"
                                            />
                                        </div>
                                    </div>

                                    <div className="space-y-2 col-span-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">URL Pública</label>
                                        <div className="relative">
                                            <Globe2 className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                            <input
                                                type="text"
                                                value={enterprise.slug || ''}
                                                onChange={(e) => handleChange('slug', e.target.value)}
                                                className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                                placeholder="Se genera automáticamente desde el nombre"
                                            />
                                        </div>
                                        <p className="text-[11px] text-slate-400 font-semibold">
                                            Se normaliza al guardar. Tu perfil público quedará en {enterprise.slug ? `/b/${enterprise.slug}` : '/b/tu-negocio'}.
                                        </p>
                                    </div>

                                    <div className="space-y-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">CIF / Identificación</label>
                                        <input
                                            type="text"
                                            value={enterprise.cif}
                                            onChange={(e) => handleChange('cif', e.target.value)}
                                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">Teléfono</label>
                                        <div className="relative">
                                            <Phone className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                            <input
                                                type="text"
                                                value={enterprise.phone}
                                                onChange={(e) => handleChange('phone', e.target.value)}
                                                className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            />
                                        </div>
                                    </div>

                                    <div className="space-y-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">Email Público</label>
                                        <div className="relative">
                                            <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                            <input
                                                type="email"
                                                value={enterprise.email}
                                                onChange={(e) => handleChange('email', e.target.value)}
                                                className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            />
                                        </div>
                                    </div>

                                    <div className="space-y-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">Sitio Web</label>
                                        <div className="relative">
                                            <Globe2 className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                            <input
                                                type="text"
                                                value={enterprise.website || ''}
                                                onChange={(e) => handleChange('website', e.target.value)}
                                                className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                                placeholder="https://tupeluqueria.com"
                                            />
                                        </div>
                                    </div>

                                    <div className="space-y-2 col-span-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">Dirección Física</label>
                                        <div className="relative">
                                            <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                            <input
                                                type="text"
                                                value={enterprise.address}
                                                onChange={(e) => handleChange('address', e.target.value)}
                                                className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                                placeholder="Calle Ejemplo 123, Ciudad"
                                            />
                                        </div>
                                    </div>

                                    <div className="space-y-2 col-span-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">Descripción corta</label>
                                        <textarea
                                            value={enterprise.description || ''}
                                            onChange={(e) => handleChange('description', e.target.value)}
                                            rows={3}
                                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all resize-none"
                                            placeholder="Cuenta un poco sobre tu negocio..."
                                        />
                                    </div>
                                </div>
                            </motion.div>
                        )}

                        {activeTab === 'branding' && (
                            <motion.div 
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                className="space-y-8"
                            >
                                <div className="space-y-6">
                                    <div className="space-y-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">URL del Logo</label>
                                        <input
                                            type="text"
                                            value={enterprise.logo || ''}
                                            onChange={(e) => handleChange('logo', e.target.value)}
                                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            placeholder="https://..."
                                        />
                                        <p className="text-[10px] text-slate-400 uppercase tracking-wider font-bold">Dimesiones recomendadas: 512x512px</p>
                                    </div>

                                    <div className="space-y-2">
                                        <label className="text-sm font-semibold text-slate-700 ml-1">URL del Banner Portada</label>
                                        <input
                                            type="text"
                                            value={enterprise.banner || ''}
                                            onChange={(e) => handleChange('banner', e.target.value)}
                                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            placeholder="https://..."
                                        />
                                        <p className="text-[10px] text-slate-400 uppercase tracking-wider font-bold">Dimesiones recomendadas: 1920x450px</p>
                                    </div>

                                    <div className="grid grid-cols-2 gap-6">
                                        <div className="space-y-2">
                                            <label className="text-sm font-semibold text-slate-700 ml-1">Color Principal</label>
                                            <div className="flex gap-3">
                                                <input
                                                    type="color"
                                                    value={enterprise.primaryColor || '#c5a059'}
                                                    onChange={(e) => handleChange('primaryColor', e.target.value)}
                                                    className="w-12 h-12 rounded-lg border-0 p-0 overflow-hidden cursor-pointer"
                                                />
                                                <input
                                                    type="text"
                                                    value={enterprise.primaryColor || '#c5a059'}
                                                    onChange={(e) => handleChange('primaryColor', e.target.value)}
                                                    className="flex-1 px-4 py-3 rounded-xl border border-slate-200 outline-none uppercase font-mono text-sm"
                                                />
                                            </div>
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-sm font-semibold text-slate-700 ml-1">Color Secundario</label>
                                            <div className="flex gap-3">
                                                <input
                                                    type="color"
                                                    value={enterprise.secondaryColor || '#1e293b'}
                                                    onChange={(e) => handleChange('secondaryColor', e.target.value)}
                                                    className="w-12 h-12 rounded-lg border-0 p-0 overflow-hidden cursor-pointer"
                                                />
                                                <input
                                                    type="text"
                                                    value={enterprise.secondaryColor || '#1e293b'}
                                                    onChange={(e) => handleChange('secondaryColor', e.target.value)}
                                                    className="flex-1 px-4 py-3 rounded-xl border border-slate-200 outline-none uppercase font-mono text-sm"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {/* Preview Card */}
                                <div className="p-6 rounded-3xl bg-slate-50 border border-slate-200 mt-8">
                                    <h4 className="text-xs uppercase font-bold tracking-widest text-slate-400 mb-4">Vista Previa Branding</h4>
                                    <div className="flex items-center gap-4">
                                        <div 
                                            className="w-12 h-12 rounded-xl flex items-center justify-center font-bold text-white shadow-lg shadow-black/10"
                                            style={{ backgroundColor: enterprise.primaryColor || '#c5a059' }}
                                        >
                                            {enterprise.name?.[0]}
                                        </div>
                                        <div 
                                            className="h-10 px-6 rounded-full flex items-center justify-center text-sm font-bold text-white shadow-md shadow-black/5"
                                            style={{ backgroundColor: enterprise.secondaryColor || '#1e293b' }}
                                        >
                                            Botón de Ejemplo
                                        </div>
                                    </div>
                                </div>
                            </motion.div>
                        )}

                        {activeTab === 'social' && (
                            <motion.div 
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                className="space-y-6"
                            >
                                <div className="space-y-2">
                                    <label className="text-sm font-semibold text-slate-700 ml-1">Instagram (Username)</label>
                                    <div className="relative">
                                        <Instagram className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                        <input
                                            type="text"
                                            value={enterprise.instagram || ''}
                                            onChange={(e) => handleChange('instagram', e.target.value)}
                                            className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            placeholder="ej. mi_barberia"
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <label className="text-sm font-semibold text-slate-700 ml-1">Facebook (Slug/Username)</label>
                                    <div className="relative">
                                        <Facebook className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                        <input
                                            type="text"
                                            value={enterprise.facebook || ''}
                                            onChange={(e) => handleChange('facebook', e.target.value)}
                                            className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <label className="text-sm font-semibold text-slate-700 ml-1">TikTok</label>
                                        <input
                                            type="text"
                                            value={enterprise.tiktok || ''}
                                            onChange={(e) => handleChange('tiktok', e.target.value)}
                                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            placeholder="@mi_cuenta"
                                        />
                                </div>

                                <div className="space-y-2">
                                    <label className="text-sm font-semibold text-slate-700 ml-1">WhatsApp (Teléfono internacional)</label>
                                        <input
                                            type="text"
                                            value={enterprise.whatsapp || ''}
                                            onChange={(e) => handleChange('whatsapp', e.target.value)}
                                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                                            placeholder="+34600111222"
                                        />
                                </div>
                            </motion.div>
                        )}

                        {activeTab === 'schedule' && (
                            <motion.div 
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                className="space-y-6"
                            >
                                <div>
                                    <h3 className="text-xl font-bold text-slate-900">Horario de Apertura</h3>
                                    <p className="text-slate-500 text-sm mb-6">Configura el horario base de tu peluquería. Este horario servirá de referencia para tus clientes.</p>
                                    {user?.enterpriseId && (
                                        <WorkingHourSettings enterpriseId={user.enterpriseId} />
                                    )}
                                </div>
                            </motion.div>
                        )}
                    </form>
                </div>
            </div>
        </section>
    );
};

export default EnterprisePage;
