-- =============================================================================
-- 1. TIPOS ENUM
-- =============================================================================
CREATE TYPE papel_usuario AS ENUM ('LEITOR', 'OPERADOR');
CREATE TYPE tipo_movimentacao AS ENUM ('ENTRADA', 'SAIDA', 'ACERTO');

-- =============================================================================
-- 2. TABELAS
-- =============================================================================

-- Perfis de Usuários (Integração com auth.users do Supabase)
CREATE TABLE perfis (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nome TEXT NOT NULL,
    email TEXT NOT NULL,
    eh_administrador BOOLEAN DEFAULT FALSE
);

-- Grupos de Materiais
CREATE TABLE grupos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome TEXT NOT NULL UNIQUE,
	icone_res TEXT
);

-- Depósitos Físicos
CREATE TABLE depositos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome TEXT NOT NULL,
    endereco TEXT
);

-- Permissões por Depósito
CREATE TABLE permissoes_usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario UUID REFERENCES perfis(id) ON DELETE CASCADE,
    id_deposito BIGINT REFERENCES depositos(id) ON DELETE CASCADE,
    papel papel_usuario NOT NULL,
    UNIQUE(id_usuario, id_deposito)
);

-- Materiais
CREATE TABLE materiais (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_alpha TEXT NOT NULL,
    nome TEXT NOT NULL,
    unidade_medida TEXT NOT NULL,
    id_grupo BIGINT REFERENCES grupos(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_materiais_id_grupo
ON materiais (id_grupo);

-- Estoque por Depósito
CREATE TABLE estoques (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_material BIGINT REFERENCES materiais(id) ON DELETE CASCADE,
    id_deposito BIGINT REFERENCES depositos(id) ON DELETE CASCADE,
    quantidade DOUBLE PRECISION DEFAULT 0.0,
    quantidade_minima DOUBLE PRECISION DEFAULT 0.0,
    UNIQUE(id_material, id_deposito)
);

-- Histórico de Movimentações
CREATE TABLE movimentacoes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    tipo tipo_movimentacao NOT NULL,
    id_material BIGINT REFERENCES materiais(id),
    id_deposito BIGINT REFERENCES depositos(id),
    id_usuario UUID REFERENCES perfis(id),
    quantidade DOUBLE PRECISION NOT NULL,
    motivo TEXT
);

-- =============================================================================
-- 3. VIEWS
-- =============================================================================

-- View 1: Resumo Geral de Materiais (com o grupo completo)
CREATE OR REPLACE VIEW visao_resumo_materiais_grupos AS
SELECT 
    m.id AS id_material,
    m.codigo_alpha,
    m.nome AS nome,
    m.unidade_medida,
    g.id AS id_grupo,
    g.nome AS nome_grupo,
	g.icone_res AS icone_grupo
FROM materiais m
JOIN grupos g ON m.id_grupo = g.id;

-- View 2: Resumo Geral de Materiais (com o estoque de depósitos específicos)
CREATE OR REPLACE VIEW visao_resumo_materiais_depositos
WITH (security_invoker = true)
AS
SELECT 
    e.id AS id_estoque,
    m.id AS id_material,
    m.codigo_alpha,
    m.nome AS nome_material,
    m.unidade_medida,
    g.id AS id_grupo,
    g.nome AS nome_grupo,
	g.icone_res AS icone_grupo,
    d.id AS id_deposito,
    d.nome AS nome_deposito,
    e.quantidade,
    e.quantidade_minima
FROM estoques e
JOIN materiais m ON e.id_material = m.id
JOIN grupos g ON m.id_grupo = g.id
JOIN depositos d ON e.id_deposito = d.id;

-- View 3: Resumo Geral de Materiais (com soma de estoque de todos os depósitos)
CREATE OR REPLACE VIEW visao_resumo_materiais AS
SELECT 
    m.id AS id_material,
    m.codigo_alpha,
    m.nome AS nome_material,
    m.unidade_medida,
    g.id AS id_grupo,
    g.nome AS nome_grupo,
	g.icone_res AS icone_grupo,
    COALESCE(SUM(e.quantidade), 0.0) AS estoque_total
FROM materiais m
JOIN grupos g ON m.id_grupo = g.id
LEFT JOIN estoques e ON m.id = e.id_material
GROUP BY 
    m.id, 
    m.codigo_alpha, 
    m.nome, 
    m.unidade_medida, 
    g.id, 
    g.nome,
	g.icone_res;

-- View 4: Itens com Estoque Baixo (com nomes e percentual crítico)
CREATE OR REPLACE VIEW visao_itens_estoque_baixo
WITH (security_invoker = true)
AS
SELECT 
    e.id,
    e.id_material,
    m.nome AS nome_material,
    e.id_deposito,
    d.nome AS nome_deposito,
    e.quantidade,
    e.quantidade_minima,
    ROUND(
        (e.quantidade / NULLIF(e.quantidade_minima, 0) * 100)::numeric, 2
    )::double precision AS percentual_restante,
    m.codigo_alpha
FROM estoques e
JOIN materiais m ON e.id_material = m.id
JOIN depositos d ON e.id_deposito = d.id
WHERE e.quantidade < e.quantidade_minima;

-- View 5: Contagem de materiais por grupo
CREATE OR REPLACE VIEW visao_grupos_com_contagem
WITH (security_invoker = true)
AS
SELECT
    g.id,
    g.nome,
    g.icone_res,
    COUNT(m.id)::BIGINT AS quantidade_materiais
FROM public.grupos g
LEFT JOIN public.materiais m ON m.id_grupo = g.id
GROUP BY g.id, g.nome, g.icone_res
ORDER BY quantidade_materiais DESC, g.nome ASC;

-- =============================================================================
-- 4. FUNCTIONS / RPC
-- =============================================================================

-- Function para os contadores do Dashboard
CREATE OR REPLACE FUNCTION obter_resumo_dashboard()
RETURNS TABLE (
    total_depositos BIGINT,
    total_grupos BIGINT,
    total_materiais BIGINT,
    total_materiais_baixo_estoque BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM depositos) AS total_depositos,
        (SELECT COUNT(*) FROM grupos) AS total_grupos,
        (SELECT COUNT(*) FROM materiais) AS total_materiais,
        (SELECT COUNT(*) FROM estoques WHERE quantidade < quantidade_minima) AS total_materiais_baixo_estoque;
END;
$$ LANGUAGE plpgsql;


-- 1. Função que será chamada pelo Trigger
CREATE OR REPLACE FUNCTION public.criar_perfil_novo_usuario()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.perfis (id, nome, email, eh_administrador)
    VALUES (
        NEW.id,
        COALESCE(
            NEW.raw_user_meta_data->>'full_name',  -- Retornado pelo Google OAuth
            NEW.raw_user_meta_data->>'name',       -- Alternativa comum
            NEW.raw_user_meta_data->>'nome',       -- Passado manualmente no e-mail/senha
            'Usuário Sem Nome'                     -- Fallback padrão
        ),
        NEW.email,
        FALSE -- Por padrão, não é administrador
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Trigger atrelado à tabela auth.users
CREATE OR REPLACE TRIGGER ao_criar_novo_usuario
AFTER INSERT ON auth.users
FOR EACH ROW
EXECUTE FUNCTION public.criar_perfil_novo_usuario();


-- =============================================================================
-- 1. HABILITAR RLS EM TODAS AS TABELAS
-- =============================================================================
ALTER TABLE perfis ENABLE ROW LEVEL SECURITY;
ALTER TABLE grupos ENABLE ROW LEVEL SECURITY;
ALTER TABLE depositos ENABLE ROW LEVEL SECURITY;
ALTER TABLE permissoes_usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE materiais ENABLE ROW LEVEL SECURITY;
ALTER TABLE estoques ENABLE ROW LEVEL SECURITY;
ALTER TABLE movimentacoes ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- 2. FUNÇÃO AUXILIAR PARA VERIFICAR SE É ADMINISTRADOR
-- =============================================================================
-- Evita ter que repetir subconsultas complexas dentro das politicas RLS
CREATE OR REPLACE FUNCTION public.eh_admin()
RETURNS boolean
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  RETURN COALESCE(
    (
      SELECT p.eh_administrador
      FROM public.perfis AS p
      WHERE p.id = auth.uid()
    ),
    false
  );
END;
$$;

CREATE OR REPLACE FUNCTION public.obter_depositos_com_acesso()
RETURNS TABLE (
    id BIGINT,
    nome TEXT,
    endereco TEXT,
    papel public.papel_usuario
)
LANGUAGE SQL
STABLE
SECURITY INVOKER
SET search_path = public, pg_temp
AS $$
    SELECT
        d.id,
        d.nome,
        d.endereco,
        CASE
            WHEN public.eh_admin() THEN NULL::public.papel_usuario
            ELSE pu.papel
        END AS papel
    FROM public.depositos d
    LEFT JOIN public.permissoes_usuario pu
        ON pu.id_deposito = d.id
       AND pu.id_usuario = auth.uid()
    WHERE public.eh_admin()
       OR pu.id_deposito IS NOT NULL
    ORDER BY d.nome;
$$;

-- =============================================================================
-- 3. POLÍTICAS: PERFIS DE USUÁRIOS
-- =============================================================================
-- Qualquer usuário autenticado pode ler os perfis
CREATE POLICY "Permitir leitura de perfis para autenticados"
ON perfis FOR SELECT TO authenticated
USING (true);

-- Apenas o próprio usuário ou administradores podem atualizar o perfil
CREATE POLICY "Permitir atualizacao do proprio perfil ou admin"
ON perfis FOR UPDATE TO authenticated
USING (id = auth.uid() OR eh_admin());

-- =============================================================================
-- 4. POLÍTICAS: GRUPOS E MATERIAIS (CADASTRO BASE)
-- =============================================================================
-- Leitura liberada para qualquer usuário logado
CREATE POLICY "Leitura de grupos liberada" ON grupos FOR SELECT TO authenticated USING (true);
CREATE POLICY "Leitura de materiais liberada" ON materiais FOR SELECT TO authenticated USING (true);

-- Apenas administradores podem criar, alterar ou excluir Grupos e Materiais
CREATE POLICY "Apenas admin modifica grupos" ON grupos FOR ALL TO authenticated USING (eh_admin());
CREATE POLICY "Apenas admin modifica materiais" ON materiais FOR ALL TO authenticated USING (eh_admin());

-- =============================================================================
-- 5. POLÍTICAS: depositos
-- =============================================================================
-- Usuários enxergam apenas depositos onde têm permissão vinculada (ou se for admin)
CREATE POLICY "Leitura de depositos permitida para usuarios vinculados ou admin"
ON depositos FOR SELECT TO authenticated
USING (
    eh_admin() OR 
    EXISTS (
        SELECT 1 FROM permissoes_usuario 
        WHERE id_usuario = auth.uid() AND id_deposito = depositos.id
    )
);

-- Apenas administradores podem criar ou gerenciar depositos
CREATE POLICY "Apenas admin gerencia depositos" ON depositos FOR INSERT TO authenticated WITH CHECK (eh_admin());
CREATE POLICY "Apenas admin altera depositos" ON depositos FOR UPDATE TO authenticated USING (eh_admin());
CREATE POLICY "Apenas admin deleta depositos" ON depositos FOR DELETE TO authenticated USING (eh_admin());

-- =============================================================================
-- 6. POLÍTICAS: PERMISSÕES DE USUÁRIO
-- =============================================================================
-- Usuário visualiza suas próprias permissões ou admin visualiza todas
CREATE POLICY "Ver proprias permissoes"
ON permissoes_usuario FOR SELECT TO authenticated
USING (id_usuario = auth.uid() OR eh_admin());

-- Apenas admin atribui ou remove permissões
CREATE POLICY "Apenas admin gerencia permissoes"
ON public.permissoes_usuario
FOR ALL
TO authenticated
USING (public.eh_admin())
WITH CHECK (public.eh_admin());

-- =============================================================================
-- 7. POLÍTICAS: ESTOQUES E MOVIMENTAÇÕES
-- =============================================================================
-- Leitura de estoque permitida se o usuário tiver acesso ao Depósito correspondente
CREATE POLICY "Ver estoque dos depositos autorizados"
ON estoques FOR SELECT TO authenticated
USING (
    eh_admin() OR 
    EXISTS (
        SELECT 1 FROM permissoes_usuario 
        WHERE id_usuario = auth.uid()
          AND id_deposito = estoques.id_deposito
          AND papel IN ('LEITOR', 'OPERADOR')
    )
);

-- Apenas OPERADORES daquele depósito específico ou ADMINS podem registrar movimentações
CREATE POLICY "Operadores podem inserir movimentacoes"
ON movimentacoes FOR INSERT TO authenticated
WITH CHECK (
    eh_admin() OR 
    EXISTS (
        SELECT 1 FROM permissoes_usuario 
        WHERE id_usuario = auth.uid() 
          AND id_deposito = movimentacoes.id_deposito 
          AND papel = 'OPERADOR'
    )
);

-- Leitura de movimentações liberada conforme acesso ao Depósito
CREATE POLICY "Ver movimentacoes dos depositos autorizados"
ON movimentacoes FOR SELECT TO authenticated
USING (
    eh_admin() OR 
    EXISTS (
        SELECT 1 FROM permissoes_usuario 
        WHERE id_usuario = auth.uid() AND id_deposito = movimentacoes.id_deposito
    )
);

-- Alterar saldo de estoque (upsert/update) permitido apenas para operadores ou admin
CREATE POLICY "Operadores podem atualizar saldo de estoque"
ON estoques FOR ALL TO authenticated
USING (
    eh_admin() OR 
    EXISTS (
        SELECT 1 FROM permissoes_usuario 
        WHERE id_usuario = auth.uid() 
          AND id_deposito = estoques.id_deposito 
          AND papel = 'OPERADOR'
    )
);

GRANT USAGE ON SCHEMA public TO authenticated;

GRANT SELECT ON TABLE
    public.perfis,
    public.grupos,
    public.depositos,
    public.permissoes_usuario,
    public.materiais,
    public.estoques,
    public.movimentacoes
TO authenticated;

GRANT SELECT ON TABLE
    public.visao_resumo_materiais_grupos,
    public.visao_resumo_materiais_depositos,
    public.visao_resumo_materiais,
    public.visao_itens_estoque_baixo,
    public.visao_grupos_com_contagem
TO authenticated;

GRANT INSERT, UPDATE, DELETE ON TABLE public.permissoes_usuario
TO authenticated;

GRANT INSERT, UPDATE ON TABLE public.estoques
TO authenticated;

GRANT INSERT ON TABLE public.movimentacoes
TO authenticated;

GRANT EXECUTE ON FUNCTION public.eh_admin()
TO authenticated;

REVOKE ALL ON FUNCTION public.obter_depositos_com_acesso()
FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.obter_depositos_com_acesso()
TO authenticated;

-- =============================================================================
-- FUNÇÕES
-- =============================================================================
CREATE OR REPLACE FUNCTION public.executar_movimentacao_estoque(
    p_tipo public.tipo_movimentacao,
    p_id_material BIGINT,
    p_id_deposito BIGINT,
    p_quantidade DOUBLE PRECISION,
    p_motivo TEXT DEFAULT NULL
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public, pg_temp
AS $$
DECLARE
    v_id_usuario UUID := auth.uid();
    v_estoque public.estoques%ROWTYPE;
    v_nova_quantidade DOUBLE PRECISION;
BEGIN
    IF v_id_usuario IS NULL THEN
        RAISE EXCEPTION 'Usuário não autenticado.';
    END IF;

    IF p_quantidade IS NULL
       OR p_quantidade <= 0
       OR p_quantidade::TEXT IN ('NaN', 'Infinity', '-Infinity') THEN
        RAISE EXCEPTION 'Quantidade deve ser um número finito maior que zero.';
    END IF;

    INSERT INTO public.estoques (
        id_material,
        id_deposito,
        quantidade,
        quantidade_minima
    )
    VALUES (p_id_material, p_id_deposito, 0, 0)
    ON CONFLICT (id_material, id_deposito) DO NOTHING;

    SELECT *
    INTO v_estoque
    FROM public.estoques
    WHERE id_material = p_id_material
      AND id_deposito = p_id_deposito
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Estoque não encontrado ou sem permissão para o depósito.';
    END IF;

    CASE p_tipo
        WHEN 'ENTRADA' THEN
            v_nova_quantidade := v_estoque.quantidade + p_quantidade;
        WHEN 'SAIDA' THEN
            v_nova_quantidade := v_estoque.quantidade - p_quantidade;
            IF v_nova_quantidade < 0 THEN
                RAISE EXCEPTION 'Estoque insuficiente.';
            END IF;
        WHEN 'ACERTO' THEN
            v_nova_quantidade := p_quantidade;
        ELSE
            RAISE EXCEPTION 'Tipo de movimentação inválido.';
    END CASE;

    UPDATE public.estoques
    SET quantidade = v_nova_quantidade
    WHERE id = v_estoque.id;

    INSERT INTO public.movimentacoes (
        tipo,
        id_material,
        id_deposito,
        id_usuario,
        quantidade,
        motivo
    )
    VALUES (
        p_tipo,
        p_id_material,
        p_id_deposito,
        v_id_usuario,
        p_quantidade,
        p_motivo
    );
END;
$$;

REVOKE ALL ON FUNCTION public.executar_movimentacao_estoque(
    public.tipo_movimentacao,
    BIGINT,
    BIGINT,
    DOUBLE PRECISION,
    TEXT
) FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.executar_movimentacao_estoque(
    public.tipo_movimentacao,
    BIGINT,
    BIGINT,
    DOUBLE PRECISION,
    TEXT
) TO authenticated;

create or replace function public.transferir_estoque(
  p_id_material bigint,
  p_id_origem bigint,
  p_id_destino bigint,
  p_quantidade numeric,
  p_id_usuario uuid,
  p_motivo text default null
)
returns void
language plpgsql
as $$
declare
  v_estoque_origem record;
  v_estoque_destino record;
  v_nova_quantidade_origem numeric;
  v_nova_quantidade_destino numeric;
begin
  if p_quantidade <= 0 then
    raise exception 'Quantidade deve ser maior que zero.';
  end if;

  if p_id_origem = p_id_destino then
    raise exception 'Origem e destino devem ser diferentes.';
  end if;

  select *
    into v_estoque_origem
  from public.estoques
  where id_material = p_id_material
    and id_deposito = p_id_origem
  for update;

  if not found then
    raise exception 'Material não encontrado no depósito de origem.';
  end if;

  if v_estoque_origem.quantidade < p_quantidade then
    raise exception 'Estoque insuficiente no depósito de origem.';
  end if;

  select *
    into v_estoque_destino
  from public.estoques
  where id_material = p_id_material
    and id_deposito = p_id_destino
  for update;

  if not found then
    insert into public.estoques (id_material, id_deposito, quantidade, quantidade_minima)
    values (p_id_material, p_id_destino, 0, 0)
    returning * into v_estoque_destino;
  end if;

  v_nova_quantidade_origem := v_estoque_origem.quantidade - p_quantidade;
  v_nova_quantidade_destino := coalesce(v_estoque_destino.quantidade, 0) + p_quantidade;

  update public.estoques
  set quantidade = v_nova_quantidade_origem
  where id = v_estoque_origem.id;

  update public.estoques
  set quantidade = v_nova_quantidade_destino
  where id = v_estoque_destino.id;

  insert into public.movimentacoes (tipo, id_material, id_deposito, id_usuario, quantidade, motivo)
  values ('SAIDA', p_id_material, p_id_origem, p_id_usuario, p_quantidade, coalesce(p_motivo, 'Transferência'));

  insert into public.movimentacoes (tipo, id_material, id_deposito, id_usuario, quantidade, motivo)
  values ('ENTRADA', p_id_material, p_id_destino, p_id_usuario, p_quantidade, coalesce(p_motivo, 'Transferência'));
end;
$$;
