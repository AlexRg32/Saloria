import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate, Link } from 'react-router-dom';

const registerSchema = z.object({
  name: z.string().min(2, 'El nombre debe tener al menos 2 caracteres'),
  email: z.string().email('Email inválido'),
  enterpriseName: z.string().min(1, 'El nombre de la empresa es obligatorio'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
  confirmPassword: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Las contraseñas no coinciden",
  path: ["confirmPassword"],
});

type ProRegisterFormValues = z.infer<typeof registerSchema>;

export const ProRegisterForm: React.FC = () => {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProRegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: ProRegisterFormValues) => {
    setIsSubmitting(true);
    setError(null);
    try {
      await registerUser({
        name: data.name,
        email: data.email,
        password: data.password,
        enterpriseName: data.enterpriseName,
      });
      navigate('/admin/dashboard', { replace: true });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al registrarse. Inténtalo de nuevo.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="w-full max-w-md p-8 bg-[#1e293b]/70 backdrop-blur-xl rounded-3xl shadow-[0_20px_50px_rgba(0,0,0,0.3)] border border-slate-700/50">
      <div className="text-center mb-10">
        <h2 className="font-display text-4xl font-black text-white tracking-tight mb-2">Registro Pro</h2>
        <p className="text-slate-400 font-medium">Lleva tu negocio al siguiente nivel</p>
      </div>

      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        {error && (
          <div className="p-4 text-sm text-red-400 bg-red-400/10 border border-red-400/20 rounded-2xl">
            {error}
          </div>
        )}

        <div>
          <label htmlFor="name" className="block text-sm font-bold text-slate-300 ml-1 mb-1">
            Nombre del Propietario
          </label>
          <input
            id="name"
            type="text"
            {...register('name')}
            className={`block w-full px-5 py-4 bg-slate-900/50 border ${
              errors.name ? 'border-red-500' : 'border-slate-700'
            } rounded-2xl text-white focus:outline-none focus:ring-4 focus:ring-brand-primary/10 transition-all duration-200 placeholder:text-slate-500`}
            placeholder="Ej: Juan Pérez"
          />
          {errors.name && <p className="mt-1 text-xs font-bold text-red-500 pr-2 text-right">{errors.name.message}</p>}
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-bold text-slate-300 ml-1 mb-1">
            Email Profesional
          </label>
          <input
            id="email"
            type="email"
            {...register('email')}
            className={`block w-full px-5 py-4 bg-slate-900/50 border ${
              errors.email ? 'border-red-500' : 'border-slate-700'
            } rounded-2xl text-white focus:outline-none focus:ring-4 focus:ring-brand-primary/10 transition-all duration-200 placeholder:text-slate-500`}
            placeholder="correo@empresa.com"
          />
          {errors.email && <p className="mt-1 text-xs font-bold text-red-500 pr-2 text-right">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="enterpriseName" className="block text-sm font-bold text-slate-300 ml-1 mb-1">
            Nombre de la Peluquería
          </label>
          <input
            id="enterpriseName"
            type="text"
            {...register('enterpriseName')}
            className={`block w-full px-5 py-4 bg-slate-900/50 border ${
              errors.enterpriseName ? 'border-red-500' : 'border-slate-700'
            } rounded-2xl text-white focus:outline-none focus:ring-4 focus:ring-brand-primary/10 transition-all duration-200 placeholder:text-slate-500`}
            placeholder="Ej: Barbería Estilo"
          />
          {errors.enterpriseName && <p className="mt-1 text-xs font-bold text-red-500 pr-2 text-right">{errors.enterpriseName.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label htmlFor="password" className="block text-sm font-bold text-slate-300 ml-1 mb-1">
              Contraseña
            </label>
            <input
              id="password"
              type="password"
              {...register('password')}
              className={`block w-full px-5 py-3.5 bg-slate-900/50 border ${
                errors.password ? 'border-red-500' : 'border-slate-700'
              } rounded-2xl text-white focus:outline-none focus:ring-4 focus:ring-brand-primary/10 transition-all duration-200 placeholder:text-slate-500`}
              placeholder="••••"
            />
            {errors.password && <p className="mt-1 text-xs font-bold text-red-500">{errors.password.message}</p>}
          </div>
          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-bold text-slate-300 ml-1 mb-1">
              Repetir
            </label>
            <input
              id="confirmPassword"
              type="password"
              {...register('confirmPassword')}
              className={`block w-full px-5 py-3.5 bg-slate-900/50 border ${
                errors.confirmPassword ? 'border-red-500' : 'border-slate-700'
              } rounded-2xl text-white focus:outline-none focus:ring-4 focus:ring-brand-primary/10 transition-all duration-200 placeholder:text-slate-500`}
              placeholder="••••"
            />
            {errors.confirmPassword && <p className="mt-1 text-xs font-bold text-red-500">{errors.confirmPassword.message}</p>}
          </div>
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full flex justify-center py-4 px-6 border border-transparent rounded-2xl shadow-xl text-lg font-bold text-slate-900 bg-brand-primary hover:scale-[1.02] active:scale-[0.98] focus:outline-none focus:ring-4 focus:ring-brand-primary/30 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed mt-4"
        >
          {isSubmitting ? (
            <div className="w-6 h-6 border-3 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
          ) : (
            'Registrar Empresa'
          )}
        </button>
      </form>

      <div className="text-center pt-4">
        <p className="text-sm text-slate-400">
          ¿Ya tienes cuenta profesional?{' '}
          <Link to="/pro/login" className="font-semibold text-brand-primary hover:opacity-80 transition-colors">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  );
};

export default ProRegisterForm;
