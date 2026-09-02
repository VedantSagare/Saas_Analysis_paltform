import { createContext, useContext, useMemo, useState } from 'react';

const TenantContext = createContext(null);

export const tenants = [
	{ id: 1, name: 'Northstar Labs', slug: 'northstar', initials: 'NL', tone: 'coral' },
	{ id: 2, name: 'Aperture Health', slug: 'aperture', initials: 'AH', tone: 'mint' },
	{ id: 3, name: 'Lumen Commerce', slug: 'lumen', initials: 'LC', tone: 'amber' },
];

export function TenantProvider({ children }) {
	const [tenantId, setTenantId] = useState(() => Number(localStorage.getItem('tenantId')) || tenants[0].id);
	const tenant = tenants.find((item) => item.id === tenantId) || tenants[0];

	const value = useMemo(() => ({
		tenant,
		tenants,
		setTenant: (id) => {
			setTenantId(Number(id));
			localStorage.setItem('tenantId', String(id));
		},
	}), [tenant]);

	return <TenantContext.Provider value={value}>{children}</TenantContext.Provider>;
}

export function useTenant() {
	return useContext(TenantContext);
}
