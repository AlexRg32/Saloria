import { useEffect, useState } from 'react';
import { userService, User, CreateUserPayload, UpdateUserPayload } from '../services/userService';
import { useAuth } from '../features/auth/hooks/useAuth';
import { EmployeeTable } from '../components/employees/EmployeeTable';
import { EmployeeModal } from '../components/employees/EmployeeModal';
import { EmployeeScheduleModal } from '../components/employees/EmployeeScheduleModal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { Plus, Search, Users as UsersIcon, RefreshCw } from 'lucide-react';

const UsersPage = () => {
    const { user: currentUser } = useAuth();
    const enterpriseId = currentUser?.enterpriseId;

    const [employees, setEmployees] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedEmployee, setSelectedEmployee] = useState<User | null>(null);
    const [employeeForSchedule, setEmployeeForSchedule] = useState<User | null>(null);
    const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
    const [employeeToDelete, setEmployeeToDelete] = useState<number | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    const fetchEmployees = async () => {
        if (!enterpriseId) return;
        try {
            setLoading(true);
            const data = await userService.getUsersByEnterprise(enterpriseId);
            setEmployees(data);
            setError(null);
        } catch (err) {
            console.error(err);
            setError('Error al cargar la lista de personal.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (enterpriseId) {
            fetchEmployees();
        }
    }, [enterpriseId]);

    const handleCreateOrUpdate = async (userData: Partial<User>) => {
        try {
            if (selectedEmployee) {
                const payload: UpdateUserPayload = {
                    name: userData.name || '',
                    email: userData.email || '',
                    role: userData.role || 'EMPLEADO',
                    phone: userData.phone,
                    active: userData.active
                };

                if (userData.password?.trim()) {
                    payload.password = userData.password;
                }

                await userService.updateUser(selectedEmployee.id, payload);
            } else {
                if (!enterpriseId) {
                    throw new Error('No se ha detectado una empresa válida para este usuario.');
                }

                const payload: CreateUserPayload = {
                    name: userData.name || '',
                    email: userData.email || '',
                    password: userData.password || '',
                    role: userData.role || 'EMPLEADO',
                    phone: userData.phone,
                    enterpriseId
                };

                await userService.createUser(payload);
            }
            await fetchEmployees();
            setIsModalOpen(false);
        } catch (err: any) {
            console.error('Error in handleCreateOrUpdate:', err);
            const msg = typeof err.response?.data === 'string' 
                ? err.response.data 
                : err.response?.data?.message || "Error al procesar la solicitud";
            alert(msg);
            throw err;
        }
    };

    const handleDelete = (id: number) => {
        setEmployeeToDelete(id);
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        if (!employeeToDelete) return;
        
        try {
            setIsDeleting(true);
            await userService.deleteUser(employeeToDelete);
            setEmployees(employees.filter(e => e.id !== employeeToDelete));
            setIsDeleteModalOpen(false);
        } catch (err) {
            console.error(err);
            alert('Error al eliminar el usuario');
        } finally {
            setIsDeleting(false);
            setEmployeeToDelete(null);
        }
    };

    const filteredEmployees = employees.filter(e => 
        e.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
        e.email.toLowerCase().includes(searchQuery.toLowerCase())
    );

    if (!currentUser) return null;

    return (
        <section className="max-w-7xl mx-auto space-y-8 pb-12 animate-in fade-in slide-in-from-bottom-4 duration-700">
            <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                <div className="space-y-1">
                     <div className="flex items-center gap-2 text-brand-primary font-semibold text-sm uppercase tracking-wider">
                        <UsersIcon size={16} />
                        Administración
                     </div>
                     <h1 className="text-4xl font-bold text-slate-900 tracking-tight">Gestión de Personal</h1>
                     <p className="text-slate-500">Administra los empleados y administradores de tu negocio.</p>
                </div>
                <div className="flex items-center gap-3">
                    <button 
                        onClick={fetchEmployees}
                        className="p-2.5 bg-white border border-slate-200 text-slate-500 hover:text-brand-primary rounded-xl transition-all active:scale-95 shadow-sm"
                        title="Refrescar"
                    >
                        <RefreshCw size={20} className={loading ? 'animate-spin' : ''} />
                    </button>
                    <button 
                        onClick={() => {
                            setSelectedEmployee(null);
                            setIsModalOpen(true);
                        }}
                        className="flex items-center gap-2 bg-brand-primary hover:bg-brand-primary/90 text-white px-6 py-2.5 rounded-xl font-bold shadow-brand transition-all active:scale-95"
                    >
                        <Plus size={20} />
                        Nuevo Empleado
                    </button>
                </div>
            </header>

            <div className="relative">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                <input 
                    type="text"
                    placeholder="Buscar por nombre o email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-12 pr-4 py-4 bg-white border border-slate-200 rounded-2xl shadow-sm focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all text-slate-700"
                />
            </div>

            {error && (
                <div className="p-4 bg-red-50 text-red-700 border border-red-100 rounded-2xl flex items-center gap-3">
                    <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse"></div>
                    {error}
                </div>
            )}

            {!enterpriseId && !loading && (
                  <div className="p-8 bg-amber-50 border border-amber-100 rounded-3xl text-amber-900 text-center">
                    <h3 className="text-lg font-bold">Empresa no detectada</h3>
                    <p className="mt-1 opacity-80">No hemos podido identificar tu empresa. Por favor, cierra sesión y vuelve a entrar.</p>
                </div>
            )}

            {loading ? (
                <div className="space-y-4">
                    {[1, 2, 3].map(i => (
                        <div key={i} className="h-20 bg-slate-100 rounded-2xl animate-pulse"></div>
                    ))}
                </div>
            ) : filteredEmployees.length === 0 ? (
                <div className="text-center py-20 bg-slate-50 rounded-3xl border border-slate-100 border-dashed">
                    <div className="mx-auto w-20 h-20 bg-white rounded-full flex items-center justify-center mb-6 shadow-sm">
                         <UsersIcon className="w-10 h-10 text-slate-200" />
                    </div>
                    <h3 className="text-xl font-bold text-slate-900">
                        {searchQuery ? 'No se encontraron resultados' : 'Aún no hay personal registrado'}
                    </h3>
                    <p className="text-slate-500 mt-2 max-w-sm mx-auto">
                        {searchQuery 
                            ? `No hay coincidencias para "${searchQuery}". Intenta con otro término.`
                            : 'Comienza agregando a los miembros de tu equipo para gestionar sus citas y horarios.'}
                    </p>
                </div>
            ) : (
                <EmployeeTable 
                    employees={filteredEmployees}
                    onEdit={(employee) => {
                        setSelectedEmployee(employee);
                        setIsModalOpen(true);
                    }}
                    onDelete={handleDelete}
                    onSchedule={(employee) => {
                        setEmployeeForSchedule(employee);
                        setIsScheduleModalOpen(true);
                    }}
                />
            )}

            <EmployeeModal 
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={handleCreateOrUpdate}
                employee={selectedEmployee}
            />

            <EmployeeScheduleModal 
                isOpen={isScheduleModalOpen}
                onClose={() => setIsScheduleModalOpen(false)}
                employee={employeeForSchedule}
            />

            <ConfirmModal 
                isOpen={isDeleteModalOpen}
                onClose={() => setIsDeleteModalOpen(false)}
                onConfirm={confirmDelete}
                title="¿Eliminar usuario definitivamente?"
                message="¡Atención! Al eliminar este usuario se eliminarán también todas sus citas asociadas y registros vinculados. Esta acción es permanente y no se puede deshacer."
                confirmText="Eliminar permanentemente"
                cancelText="Mantener usuario"
                variant="danger"
                isLoading={isDeleting}
            />
        </section>
    );
};

export default UsersPage;
